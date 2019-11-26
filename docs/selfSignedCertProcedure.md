# Self-signed certificate creation

## Procedure

With terminal, go to KARAF_ROOT/etc and run:
- mkdir keystores
- cd keystores
- keytool -genkey -keyalg RSA -alias selfsigned -keystore keystore -storepass password -validity 360 -keysize 2048
- reRun Karaf

