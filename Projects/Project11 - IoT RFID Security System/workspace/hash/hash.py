import hashlib
# for security, SHA3-256 > SHA2-256

def hash_function(string, salt):
    hash_ = hashlib.sha3_256()
    string = string + salt
    string = string.encode('utf-8')
    hash_.update(string)
    hash_value = hash_.hexdigest()
    return hash_value
