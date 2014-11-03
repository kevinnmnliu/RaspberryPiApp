import web
import json
import ast
from bootstrap import *

urls = (
	'/rpi', 'rpi'
)

class rpi:
	def GET(self):
		return "web.py up and running"
	def POST(self):
		data = web.data()
		print "data: "+data
		# dictData = ast.literal_eval(data)
		# print "dictionary data: "+dictData
		jsonData = json.loads(data)
		# print "jsonData: "+jsonData
		# print "lights: "+jsonData['lights']
		lightsList = jsonData['lights']
		propagate = jsonData['propagate']
		led.all_off()
		led.setMasterBrightness(1)
		if(propagate):
			idList = []
			for light in lightsList:
				idList.append(light['lightId']-1)
			for i,light in enumerate(lightsList):
				r = light['red']
				g = light['green']
				b = light['blue']
				intensity = light['intensity']
				led.setMasterBrightness(intensity)
				startId =  idList[i]
				if i >= len(idList)-1:
					endId = 31
				else:
					endId = idList[i+1]
				led.fillRGB(r,g,b,startId,endId)
				led.update()
				
		else:
			for light in lightsList:
				print str(light['red'])
				print str(light['green'])
				print str(light['blue'])
				print str(light['lightId'])
				print str(light['intensity'])
				print ""
				r = light['red']
				g = light['green']
				b = light['blue']
				intensity = light['intensity']
				led.setMasterBrightness(intensity)
				id = light['lightId']-1
				led.fillRGB(r,g,b,id,id)
				led.update()
		
if __name__ == "__main__":
	app = web.application(urls, globals())
	app.run()
