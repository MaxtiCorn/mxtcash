# mxtcash  

to run locally with docker:  
1. install docker  
2. create local image with sbt plugin  
sbt docker:publishLocal  
3. run image with docker  
docker run --rm -e HOST='{internal_host}' -e PORT='{internal_port}' -p {internal_port}:{external_port} {app_name}:{app_version}  
where  
internal_host - host of server in container (for example, 0.0.0.0)  
internal_port - port of server in container (for example, 8080)  
external_port - port to publish out (for example, 8080)  
app_name - name of application (value of name in build.sbt)  
app_version - version of application (value of version in build.sbt)  

deploy to heroku:  
1. install heroku cli, login, create app and connect  
2. deploy to heroku with sbt plugin  
sbt:deployHeroku  
