from django_filters import FilterSet, NumberFilter, CharFilter
from .models import Data

class DataFilter(FilterSet):
    value = NumberFilter() 
    gt = NumberFilter(field_name="value", lookup_expr="gt") 
    lt = NumberFilter(field_name="value", lookup_expr="lt")
    
    class Meta:
        model = Data
        fields = ["device", "value"]
    
    def __init__(self, *args, **kwargs): 
        super(DataFilter, self).__init__(*args, **kwargs)