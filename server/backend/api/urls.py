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
    path('get_phone/', views.get_phone, name='get_phone'),

    path('add_cell_info/', views.add_cell_info, name='add_cell_info'),
    path('add_test/', views.add_test, name='add_test'),

    path('get_users/', views.get_users, name='get_users'),
    path('get_cell_infos/', views.get_cell_info, name='get_cell_infos'),
    path('get_tests/', views.get_tests, name='get_tests'),

    path("download_test/", views.http_download_test, name='http_download_test'),
    path("upload_test/", views.http_upload_test, name='http_upload_test'),


]
