#!/usr/bin/env python
# coding: utf-8

# In[1]:


import numpy as np
import pandas as pd
df_1 = pd.read_csv('googleplaystore.csv')
df_1


# In[2]:


#First, we want to drop unnecessary columns: current ver and android ver. 
#The dataset didn't specify the meaning of these two columns and we believe they are not related to our analysis purpose.
df_1.drop(['Current Ver','Android Ver'], axis = 1, inplace = True)


# In[3]:


# Then we want to remove the space in the column names
df_1.rename({'Content Rating':'Content_rating','Last Updated':'Last_updated'}, axis = 1, inplace=True)
df_1


# Data cleaning:
# 1. delete nan ratings
# 2. check uniqueness of each column
# 3. delete unnecessary columns: current ver. android ver.
# 4. change content rating into age range, so can do regression
# 5. size: check how many varies with device, delete or fill with average or mode, we can delete "M"
# 6. "Installs" to numerical, histgram graph, bins
# 7. "Type" dummy variable
# 8. "Price" might need to delete dollar sign
# 9. Save only year in "last updated"
# 10. Use category, change genre to number of genres ???
# 11. create index for app or app as the index?

# In[4]:


df_1.info()


# In[5]:


#Rating is the initial Y variable.
#so we prefer to drop the nan value since this level of missing values won't affect our results largely
df_1 = df_1.dropna()
df_1.info()


# In[6]:


#We detect that there are duplicated app names, but we think it is possible since App is not a primary key.
# There are different apps using same app names, or same app with different versions, etc.
df_1['App'].nunique()


# In[7]:


#But if all the fields is duplicated, then we should remove it.
df_1.drop_duplicates(inplace=True)
df_1.info()


# In[8]:


# Now we have 8892 rows and next, we need to change the type of each column.
# Check if 'Reviews' is numeric and change it into integers
if df_1.Reviews.str.isnumeric().sum()==8892:
    df_1.Reviews = df_1.Reviews.astype(int)


# In[9]:


# There are 3 types of data in Size, ending with M, K, or 'varies with device'.
# Assuming 1 MB = 1000 KB
# Fill varies with device with the average
df_1 = df_1.reset_index(drop=True)
for i in range(len(df_1.Size)):
    if "M" in df_1.Size[i]:
        df_1.Size[i] = float(df_1.Size[i][:-1]) * 1000
    elif "k" in df_1.Size[i]:
        df_1.Size[i] = float(df_1.Size[i][:-1])
    else:
        df_1.Size[i] = np.nan
df_1.Size = round(df_1.Size.fillna(df_1.Size.mean())).astype(int)


# In[10]:


#change installs to numerical, bins for hist
df_1.Installs = df_1.Installs.str.replace(',','')
df_1.Installs = df_1.Installs.str.replace('+','')
df_1.Installs = df_1.Installs.astype(int)


# In[11]:


#change type to int first, can discuss how to use this later, or create dummy variable
# 0 - Free, 1 - Paid
df_1['Type'] = (df_1['Type'] == 'Paid').astype(int)


# In[12]:


#replace $ and change to float
df_1.Price = df_1.Price.str.replace('$','').astype(float)


# In[13]:


# I think it's not necessary to change it to age range as they are all crossed.
df_1.Content_rating.unique()


# In[14]:


# at most 2 genres, we can talk if double genres would contribute to the performance of app
df_1.Genres = df_1.Genres.str.count(';').astype(int)+1


# In[15]:


# set 2019-01-01 as the data collection date
# count the days to latest updated 
import datetime
df_1['Last_updated'] = pd.to_datetime(df_1['Last_updated'])
df_1['Days_after_last_updated'] = (datetime.datetime(2019, 1, 1) - df_1['Last_updated']).dt.days


# In[16]:


df_1.info()


# In[17]:


df_1


# In[18]:


df_1.to_excel('Google Playstore.xlsx')


# In[19]:


temp = df_1[df_1['Content_rating']=='Mature 17+'].groupby('Category').App.count().to_frame()
temp.to_excel('temp1.xlsx')


# In[20]:


temp.plot(kind='pie', y='App', autopct='%1.0f%%',figsize=(10,10))


# In[21]:


import numpy as np
binned_installs = pd.cut(df_1['Installs'],
                     bins = [0, 1001, 10001, 100001, 1000001, 10000001, 100000001, 1000000001], 
                     labels=['1000', '10000', '100000', '1000000', '10000000', '100000000', '1000000000'])
dict_binned_installs = {'bin_impr': binned_installs}
temp = df_1
temp['binned_installs'] = pd.DataFrame(dict_binned_installs)
temp.groupby('binned_installs').Installs.count().plot(kind='line',figsize=(10,8), title='Count of Installs in bins')


# In[22]:


temp.groupby('binned_installs').Rating.mean().plot(kind='line',figsize=(10,8),title='average rating of install bins')


# In[23]:


df_1.Days_after_last_updated.min()


# In[24]:


binned_updated_days = pd.cut(df_1['Days_after_last_updated'],
                     bins = [100, 200, 300, 500, 1000, 2000, np.inf], 
                     labels=['1:100-200', '2:200-300', '3:300-500', '4:500-1000', '5:1000-2000', '6:2000+'])
dict_binned_updated_days = {'bin_impr': binned_updated_days}
temp = df_1
temp['binned_updated_days'] = pd.DataFrame(dict_binned_updated_days)
temp.groupby('binned_updated_days').Installs.sum().plot(kind='line',figsize=(10,8),title='Total Installs per last updated days')


# In[25]:


temp.groupby('binned_updated_days').Rating.mean().plot(kind='line',figsize=(10,8),title='Average Rating per last updated days')


# In[26]:


import matplotlib.pyplot as plt
import seaborn as sns
plt.figure(figsize=(10, 10))
sns.heatmap(df_1.corr(), vmax=.8, linewidths=0.01,square=True,annot=True,cmap='YlGnBu' ,linecolor="white")
plt.title('Correlation between features')


# # Simple Linear Regression:
# i.Price & Installs

# In[27]:


import sklearn
from scipy import stats
from sklearn.linear_model import LinearRegression
from sklearn.metrics import r2_score
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import (StandardScaler, PolynomialFeatures)
from sklearn.preprocessing import LabelEncoder


# In[28]:


slr = LinearRegression()
X = df_1 [['Installs']]
y = df_1 [["Price"]]
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3, random_state=42)
s = StandardScaler()
X_train = s.fit_transform(X_train)
slr.fit(X_train,y_train)
X_test = s.transform(X_test)
y_pred = slr.predict(X_test)
print(f'R2 score is {r2_score(y_pred,y_test)}')
print(f'Co-efficients are {slr.coef_}')
print(f'Intercept is {slr.intercept_}')


# In[29]:


plt.figure(figsize=(10,5))
sns.regplot(X_test,y_pred,scatter_kws={"color": "red"},line_kws={"color": "green"})
plt.xlabel('Installs')
plt.ylabel('Price')

plt.show()


# ii. Rating & Days_after_last_updated

# In[30]:


slr = LinearRegression()
X = df_1 [['Days_after_last_updated']]
y = df_1 [["Rating"]]
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
s = StandardScaler()
X_train = s.fit_transform(X_train)
slr.fit(X_train,y_train)
X_test = s.transform(X_test)
y_pred = slr.predict(X_test)
print(f'R2 score is {r2_score(y_pred,y_test)}')
print(f'Co-efficients are {slr.coef_}')
print(f'Intercept is {slr.intercept_}')


# In[31]:


plt.figure(figsize=(10,5))
sns.regplot(X_test,y_pred,scatter_kws={"color": "red"},line_kws={"color": "green"})
plt.xlabel('Days_after_last_updated')
plt.ylabel('Rating')

plt.show()


# iii. Installs & Days_after_last_updated

# In[32]:


slr = LinearRegression()
X = df_1 [['Days_after_last_updated']]
y = df_1 [["Installs"]]
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
s = StandardScaler()
X_train = s.fit_transform(X_train)
slr.fit(X_train,y_train)
X_test = s.transform(X_test)
y_pred = slr.predict(X_test)
print(f'R2 score is {r2_score(y_pred,y_test)}')
print(f'Co-efficients are {slr.coef_}')
print(f'Intercept is {slr.intercept_}')


# In[33]:


plt.figure(figsize=(10,5))
sns.regplot(X_test,y_pred,scatter_kws={"color": "red"},line_kws={"color": "green"})
plt.xlabel('Days_after_last_updated')
plt.ylabel('Installs')

plt.show()


# In[34]:


df_2 = pd.read_csv('googleplaystore_user_reviews.csv')
df_2


# In[35]:


df_2 = df_2.dropna()
df_2.info()


# In[36]:


df_1.merge(df_2, how = 'inner', on = 'App').drop_duplicates()


# In[37]:


df_1.merge(df_2, how = 'inner', on = 'App').drop_duplicates().App.nunique()


# In[38]:


temp = df_1.merge(df_2, how = 'inner', on = 'App').drop_duplicates()
new = temp.groupby('App').Sentiment_Polarity.mean().to_frame().reset_index()
new2 = temp.groupby('App').Sentiment_Subjectivity.mean().to_frame().reset_index()
new = new.merge(new2, how = 'inner', on = 'App').drop_duplicates()
temp = temp[['App','Category','Reviews']]
new = new.merge(temp, how = 'inner', on = 'App').drop_duplicates()
new


# In[39]:


df1 = new.groupby('Category').App.count().to_frame().reset_index()


# In[40]:


new_temp = new[new['Sentiment_Polarity']<0]
df2 = new_temp.groupby('Category').App.count().to_frame().reset_index()


# In[41]:


df = df1.merge(df2, how='inner', on ='Category')
df['NegativePercent'] = round(df['App_y']/df['App_x'],3) * 100
df.sort_values('NegativePercent',ascending = False)


# In[42]:


df.to_excel('temp2.xlsx')


# # Multiple Linear Regression

# i: Reviews & sentiment_polarity, snetiment_subjectivity

# In[43]:


X = new [['Sentiment_Polarity','Sentiment_Subjectivity']]
y = new [["Reviews"]]
mlr = LinearRegression()
s = StandardScaler()
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42) #split into train and test parts
X_train_sm = s.fit_transform(X_train)
mlr.fit(X_train_sm, y_train)
X_test_sm = s.transform(X_test)
y_pred_sm = mlr.predict(X_test_sm)
print(f'R2 score is {r2_score(y_pred_sm,y_test)}')
print("Residual sum of squares (MSE): %.2f" % np.mean((y_pred_sm - y_test) ** 2))
print(f'Co-efficients are {mlr.coef_}')
print(f'Intercept is {mlr.intercept_}')


# ii. Rating & Size, Price, Content rating, and Days after last updated

# In[44]:


labels = df_1['Content_rating'].unique().tolist()
df_1['Content_rating'] = df_1['Content_rating'].apply(lambda x: labels.index(x))


# In[45]:


labels


# In[46]:


X = df_1 [['Size','Price','Content_rating','Days_after_last_updated']]
y = df_1 [["Rating"]]
mlr = LinearRegression()
s = StandardScaler()
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42) #split into train and test parts
X_train_sm = s.fit_transform(X_train)
mlr.fit(X_train_sm, y_train)
X_test_sm = s.transform(X_test)
y_pred_sm = mlr.predict(X_test_sm)
print(f'R2 score is {r2_score(y_pred_sm,y_test)}')
print("Residual sum of squares (MSE): %.2f" % np.mean((y_pred_sm - y_test) ** 2))
print(f'Co-efficients are {mlr.coef_}')
print(f'Intercept is {mlr.intercept_}')


# iii Installs & Rating, Size, Reviews, Price, Content rating, and Days after last updated
# 

# In[47]:


X = df_1 [['Rating','Size', 'Reviews','Price','Content_rating','Days_after_last_updated']]
y = df_1 [["Installs"]]
mlr = LinearRegression()
s = StandardScaler()
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42) #split into train and test parts
X_train_sm = s.fit_transform(X_train)
mlr.fit(X_train_sm, y_train)
X_test_sm = s.transform(X_test)
y_pred_sm = mlr.predict(X_test_sm)
print(f'R2 score is {r2_score(y_pred_sm,y_test)}')
print("Residual sum of squares (MSE): %.2f" % np.mean((y_pred_sm - y_test) ** 2))
print(f'Co-efficients are {mlr.coef_}')
print(f'Intercept is {mlr.intercept_}')


# In[ ]:




