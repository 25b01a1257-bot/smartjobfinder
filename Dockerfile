# Stage 1: Build Java Servlets
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

# Copy libraries and source code
COPY lib/ lib/
COPY src/ src/
COPY web/ web/

# Create classes directory and compile Java files
RUN mkdir -p web/WEB-INF/classes && \
    javac -encoding UTF-8 -cp "lib/*" -d web/WEB-INF/classes src/com/SmartJobFinder/*.java

# Stage 2: Runtime with Apache Tomcat 10.1 (Jakarta EE 10)
FROM tomcat:10.1-jdk17-temurin-jammy

# Clean default Tomcat webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Deploy SmartJobFinder as the ROOT webapp (accessible at /)
COPY --from=builder /app/web/ /usr/local/tomcat/webapps/ROOT/

# Add MySQL JDBC Driver to Tomcat lib
COPY lib/mysql-connector-j-26.7.0.jar /usr/local/tomcat/lib/

# Dynamic port binding for Railway / Cloud deployment (passes $PORT)
ENV PORT=8080
EXPOSE 8080

# Start Tomcat listening on assigned $PORT
CMD sh -c 'sed -i "s/port=\"8080\"/port=\"${PORT:-8080}\"/g" /usr/local/tomcat/conf/server.xml && catalina.sh run'
