from django.contrib import admin
from django.urls import path, include
from . import views

urlpatterns = [
    path('pub/', views.pub, name='pub'),
    path('publish/', views.publish, name='publish'),
    path('httprequest/', views.httprequest, name='httprequest'),
    path('image/messages', views.image, name='image'),
]
