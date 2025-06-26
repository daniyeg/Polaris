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

    path('add_cell_info/', views.add_cell_info, name='add_cell_info'),
    path('add_test/', views.add_test, name='add_test'),
    # path('add_http_download/', views.add_http_download_test, name='add_http_download'),
    # path('add_http_upload/', views.add_http_upload_test, name='add_http_upload'),
    # path('add_ping/', views.add_ping_test, name='add_ping'),
    # path('add_dns/', views.add_dns_test, name='add_dns'),
    # path('add_web/', views.add_web_test, name='add_web'),
    # path('add_sms/', views.add_sms_test, name='add_sms'),
    
    path('get_users/', views.get_users, name='get_users'),
    path('get_cell_infos/', views.get_cell_info, name='get_cell_infos'),
    path('get_tests/', views.get_tests, name='get_tests'),
    # path('get_http_downloads/', views.get_http_download_tests, name='get_http_downloads'),
    # path('get_http_uploads/', views.get_http_upload_tests, name='get_http_uploads'),
    # path('get_pings/', views.get_ping_tests, name='get_pings'),
    # path('get_dns/', views.get_dns_tests, name='get_dns'),
    # path('get_webs/', views.get_web_tests, name='get_webs'),
    # path('get_sms/', views.get_sms_tests, name='get_sms'),
]
