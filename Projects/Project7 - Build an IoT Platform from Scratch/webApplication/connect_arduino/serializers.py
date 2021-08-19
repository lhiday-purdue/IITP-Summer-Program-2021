from rest_framework.serializers import ModelSerializer, HyperlinkedModelSerializer
from .models import Data, Device, Image

class DataSerializer(ModelSerializer):
    class Meta:
        model = Data
        fields = ['device', 'value', 'time']
        
class DeviceSerializer(ModelSerializer):
    class Meta:
        model = Device
        fields = ['device1', 'device2', 'device3', 'device4']
        
class ImageSerializer(HyperlinkedModelSerializer):
    class Meta:
        model = Image
        fields = ('text', 'image')