from .models import Data, Device, Image
from rest_framework.viewsets import ModelViewSet
from rest_framework.mixins import UpdateModelMixin
from .serializers import DataSerializer, DeviceSerializer, ImageSerializer
from .filter import DataFilter
from rest_framework.response import Response
from rest_framework import status
import requests

class DataViewSet(ModelViewSet):
    """
    API endpoint that allows users to be viewed or edited.
    """
    filterset_fields = ('device', )
    filter_class = DataFilter
    queryset = Data.objects.all()
    serializer_class = DataSerializer
    
    def create(self, request, *args, **kwargs):
        serializer = self.serializer_class(data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
class DeviceViewSet(ModelViewSet):
    queryset = Device.objects.all()
    serializer_class = DeviceSerializer
    
    def update(self, request, *args, **kwargs):
        url = 'https://reqres.in/api/users/'
        payload = {"name":"test", "job":"student"}
        r = requests.post(url, data = payload)
        
        if not r.ok:
            return Response(status=status.HTTP_400_BAD_REQUEST)
        
        partial = kwargs.pop('partial', False)
        instance = self.get_object()
        serializer = self.get_serializer(instance, data=request.data, partial=partial)
        serializer.is_valid(raise_exception=True)
        self.perform_update(serializer)

        if getattr(instance, '_prefetched_objects_cache', None):
            # If 'prefetch_related' has been applied to a queryset, we need to
            # forcibly invalidate the prefetch cache on the instance.
            instance._prefetched_objects_cache = {}
            
        return Response(serializer.data, status=r.status_code)
    
class ImageViewSet(ModelViewSet):
    queryset = Image.objects.all()
    serializer_class = ImageSerializer
