from django.urls import include, path
from rest_framework.routers import DefaultRouter
from .views import DataViewSet, DeviceViewSet, ImageViewSet

router = DefaultRouter()
router.register('value', DataViewSet)
router.register('device', DeviceViewSet)
router.register('image', ImageViewSet)

urlpatterns = [
    path('', include(router.urls)),
]