import mysql.connector

data_base = mysql.connector.connect(
    host='',     
    user='',
    passwd=''
)

cursor = data_base.cursor()
cursor.execute("CREATE DATABASE polarisdb")
