from django.contrib import admin
from .models import Data, Device, Image

# Register your models here.
class showDataDetail(admin.ModelAdmin):
    list_display=('device','value','time',)

class showDeviceDetail(admin.ModelAdmin):
    list_display=('device1','device2','device3','device4')
    
class showImageDetail(admin.ModelAdmin):
    list_display=('text', 'image')

admin.site.register(Data, showDataDetail)
admin.site.register(Device, showDeviceDetail)
admin.site.register(Image, showImageDetail)