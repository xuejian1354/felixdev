./com.ymbl.smartgateway.extension/jni/compile.sh && ant
ls felix/bundle/com.ymbl.smartgateway.extension-1.0.0.jar
ls felix/bundle/com.ymbl.smartgateway.transite-1.0.0.jar
cd felix/ && java -jar bin/felix.jar
