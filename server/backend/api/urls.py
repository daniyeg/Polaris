from django.urls import path, re_path
from . import views


urlpatterns = [
    path('', views.get_item, name='get_item'),
    path('add/', views.add_item, name='add_item'),
    path('request_otp/', views.request_otp, name='request_otp'),
    path('verify_otp/', views.verify_otp, name='verify_otp'),

    path('signup/', views.signup_user, name='signup'),
    path('login/', views.login_user, name='login'),
    path('logout/', views.logout_user, name='logout'),

    path('cell_info/', views.create_cell_info, name='create_cell_info'),
    path('test/', views.create_test, name='create_test'),
    path('http_download/', views.create_http_download_test, name='http_download'),
    path('http_upload/', views.create_http_upload_test, name='http_upload'),
    path('ping/', views.create_ping_test, name='ping'),
    path('dns/', views.create_dns_test, name='dns'),
    path('web/', views.create_web_test, name='web'),
    path('sms/', views.create_sms_test, name='sms'),
    
    path('get_users/', views.get_users, name='get_users'),
    path('get_cell_info/', views.get_cell_info, name='get_cell_info'),
    path('get_tests/', views.get_tests, name='get_tests'),
    path('get_http_downloads/', views.get_http_download_tests, name='get_http_downloads'),
    path('get_http_uploads/', views.get_http_upload_tests, name='get_http_uploads'),
    path('get_pings/', views.get_ping_tests, name='get_pings'),
    path('get_dns/', views.get_dns_tests, name='get_dns'),
    path('get_webs/', views.get_web_tests, name='get_webs'),
    path('get_sms/', views.get_sms_tests, name='get_sms'),
]
