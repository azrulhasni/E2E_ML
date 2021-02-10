library(rpart)
library(rpart.plot)
library(dplyr)
library(DMwR)
library(randomForest)
library(readr)
library(pmml)
library(caret)
library(questionr)
library(Boruta)
library(jsonlite)

setwd("~/Documents/GitHub/E2E_ML")

#Inspecting our data
#--------------------

accepted_2007_to_2018Q4 <- read_csv("accepted_2007_to_2018Q4.csv")

spec(accepted_2007_to_2018Q4)

nrow(accepted_2007_to_2018Q4)

ncol(accepted_2007_to_2018Q4)

data<-accepted_2007_to_2018Q4#make a copy first

length(unique(data$id)) #how many unique ids (including NA - the empty value)

nrow(filter(data,is.na(id))) #are there any empty ids  

data<-filter(data,!is.na(id)) #only keep data that has a proper id

nrow(data)#count the number of rows of data again just to be sure

#Features and label
#------------------

unique(data$loan_status)

t1<-data$loan_status[]=="Current"|data$loan_status[]=="Fully Paid"|data$loan_status[]=="Does not meet the credit policy. Status:Charged Off"  
  
  
data$target<-t1 #create a new column called target. Target is now a logical (boolean) column  
  
data<-subset(data, select=-c(loan_status)) #take out loan_status as it will interfere with learning

#Taking out unique columns
#-------------------------

c <- data %>% summarise_all(n_distinct)
str(c)

which(c==length(unique(data$id)))

#Take out url column
data <- subset(data, select=-c(url))

#Taking out columns with only the same value throughout
#------------------------------------------------------

c <- sapply(data,function(x) length(unique(x))==1)  
d <- names(which(c==TRUE))  
print(d)

data <- data[, !colnames(data) %in% d]

#Feature selection
#-----------------
fna<-as.data.frame(freq.na(data)) #find the frequency of empty data, get it as data frame
a<-fna[fna$`%`<60,] #get the columns that is at least 60% not null

b<-data[, colnames(data) %in% rownames(a)] #filter out columns with 60% or more nulls
data4boruta <- na.omit(b) #delete any row with null in it

smplddata <- data4boruta[sample(nrow(data4boruta), 60000), ]  #sample 
boruta_output <- Boruta(target ~ ., data=smplddata, doTrace=2, maxRuns=20)   
  
boruta_signif <- getSelectedAttributes(boruta_output, withTentative = FALSE)  
  
print(boruta_signif)

data <- data[, colnames(data) %in% boruta_signif]

data$target <- t1 

#Business constraints
#--------------------
data_orig <- data #save original data  

data <- subset(data, select=-c(id,grade,sub_grade)) #take out the unneded column

data <- subset(data, select=-c(issue_d,last_credit_pull_d,last_pymnt_d)) #take out the unneded column


#Which algorithm to use
#----------------------

data <- data %>% mutate_if(is.character,as.factor)

summary(data)

#Splitting training and testing data
#-----------------------------------
data <- data[sample(nrow(data)),] #shuffle  
trainingratio <- 0.85  
traindata <- data[1:floor(nrow(data)*trainingratio),]  
u <- floor(nrow(data)*trainingratio)+1  
v <- nrow(data)  
testdata <- data[u:v,]

#Let us grow a tree
#------------------
fit <- rpart(target~.,   
            data = traindata,   
            method = 'class',  
            cp = 0.001  
)

rpart.plot(fit)


#Testing our tree
#----------------
default_predict <- predict(fit, testdata, type = 'class')

#ML model quality
#----------------
cm <- confusionMatrix(  
   as.factor(as.integer(default_predict)-1),  
   as.factor(as.integer(testdata$target)),   
   mode = "everything", positive="1")  
print(cm)

#Confusion matrix
#----------------
print(cm$table[2:1, 2:1]/nrow(testdata))

#Not all errors are made equal
#-----------------------------

loss <- matrix(c(0, 1, 5, 0), nrow = 2)  
fit_with_loss <- rpart(target~.,   
             data = traindata,   
             method = 'class',   
             cp = 0.001,  
             parms=list(loss=loss)  
 )  
default_predict_with_loss = predict(fit_with_loss, testdata, type = 'class')

cm_with_loss <- confusionMatrix(
   as.factor(as.integer(default_predict_with_loss)-1),
   as.factor(as.integer(testdata$target)), 
   mode = "everything", positive="1")
 
print(cm_with_loss$table[2:1,2:1]/nrow(testdata))

#Discussion: Imbalanced classification
#-------------------------------------
print(nrow(subset(data,data$target==FALSE))/nrow(data))
print(nrow(subset(data,data$target==TRUE))/nrow(data))

traindata1 <- subset(traindata,traindata$target==TRUE) #find the TRUE class (majority)  
  
traindata0 <- subset(traindata,traindata$target==FALSE)#find the FALSE class (minority)  
  
traindata11 <- traindata1[sample(nrow(traindata1),floor(1*nrow(traindata0))), ] #since TRUE class is majority, we sample it. The sample is as big as the minority class  
  
traindataR1 <- rbind(traindata11,traindata0) # we combile the minority class and the sample  
  
   
traindataR1 <- traindataR1[sample(nrow(traindataR1)),] #shuffle them around  
  
fit_with_undersampling <- rpart(target~.,   
               data = traindataR1,   
               method = 'class',   
               cp = 0.001  
   ) #train based on the sampled data + minority 
   
default_predict_with_undersampling <- predict(fit_with_undersampling, testdata, type = 'class')  
  
cm_with_undersampling <- confusionMatrix(  
   as.factor(as.integer(default_predict_with_undersampling)-1),  
   as.factor(as.integer(testdata$target)),   
   mode = "everything", positive="1")  
  
print(cm_with_undersampling$table[2:1,2:1]/nrow(testdata))


#Exporting our machine learning model
#------------------------------------
x<-pmml(fit)
saveXML(x,"default_model_normal.pmml")  
  
x_wl<-pmml(fit_with_loss)  
saveXML(x_wl,"default_model_lowrisk.pmml")

#Exporting test data to JSON and CSV
#----------------------------------
testdata$id <-1:nrow(testdata)#add an id
sampledtestdata <- testdata[sample(nrow(testdata),20),] #sample some 20 data points
testjson <- toJSON(sampledtestdata)
write(testjson, file="input.json")
write.csv(sampledtestdata, file="input.csv")



