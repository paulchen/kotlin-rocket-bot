#!/usr/bin/python3

import requests, sys, os

if 'WEBSERVICE_PORT' not in os.environ:
    print('Environment variable WEBSERVICE_PORT not set')
    sys.exit(3)

webservice_port = os.environ['WEBSERVICE_PORT']

r = requests.get('http://localhost:' + webservice_port + '/status')
if not r:
    print('UNKNOWN: Status code %s received from webservice' % r.status_code)
    sys.exit(3)

j = r.json()
status = j['status']
last_ping = j['lastPing']

message = ('%s: Last ping at %s' % (status, last_ping))
if status == 'CRITICAL':
    return_code = 2
elif status == 'WARNING':
    return_code = 1
elif status == 'OK':
    return_code = 0
else:
    message = 'UNKNOWN: Unknown status %s' % (status)
    return_code = 3

print(message)
if len(j['problems']) == 0:
    print('No problems detected')
else:
    print('%s problems detected:' % (len(j['problems'])))
    for problem in j['problems']:
        print('%s/%s: %s' % (problem['category'], problem['subcategory'], problem['description']))

sys.exit(return_code)
