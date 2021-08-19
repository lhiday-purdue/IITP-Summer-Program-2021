from django.db import models

# Create your models here.
class Data(models.Model):
    device = models.CharField(max_length=20)
    value = models.DecimalField(max_digits=8, decimal_places=4)
    
    time = models.DateTimeField(auto_now_add=True)
    
    class Meta:
        ordering = ['-pk']
    
    def __str__(self):
        return f'{self.device}::{self.value}'
    
class Device(models.Model):
    device1 = models.NullBooleanField()
    device2 = models.NullBooleanField()
    device3 = models.NullBooleanField()
    device4 = models.NullBooleanField()
    
    def __str__(self):
        return f'{self.device1}|{self.device2}|{self.device3}|{self.device4}'

class Image(models.Model):
    text = models.CharField(max_length=255)
    image = models.ImageField(default='media/default_image.jpeg')
    
    class Meta:
        ordering = ['-pk']