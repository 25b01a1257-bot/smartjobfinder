package com.SmartJobFinder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CompanySeedData {

    public static class CompanyDef {
        public String name;
        public String skills;
        public String role;
        public String salary;
        public String exp;
        public String desc;
        public String logo;
        public String url;
        public String status;

        public CompanyDef(String name, String skills, String role, String salary, String exp, String desc, String logo, String url) {
            this.name = name;
            this.skills = skills;
            this.role = role;
            this.salary = salary;
            this.exp = exp;
            this.desc = desc;
            this.logo = logo;
            this.url = url;
            this.status = "ACTIVE";
        }
    }

    public static List<CompanyDef> getFullCompanyList() {
        List<CompanyDef> list = new ArrayList<>();

        // 1-13: Core Originals
        list.add(new CompanyDef("Google", "Java, Python, C++, Go, Cloud, SQL, Data Structures, Algorithms, Spring", "Software Engineer", "1800000", "0-3", "Build scalable distributed systems, cloud infrastructure, and search engineering pipelines using modern Java and Go.", "images/google.svg", "https://careers.google.com"));
        list.add(new CompanyDef("Deloitte", "Java, SQL, Spring Boot, Web Development, HTML, CSS, JavaScript, React", "Technology Analyst", "850000", "0-2", "Analyze, design, and develop enterprise cloud and web applications for global Fortune 500 financial and tech clients.", "images/deloitte.svg", "https://www2.deloitte.com/global/en/careers.html"));
        list.add(new CompanyDef("Microsoft", "C#, Java, Azure, SQL, React, TypeScript, Cloud, Python", "Software Engineer", "1700000", "1-3", "Develop robust cloud services, Azure infrastructure solutions, and cross-platform enterprise client software.", "images/microsoft.svg", "https://careers.microsoft.com"));
        list.add(new CompanyDef("Amazon", "Java, AWS, Distributed Systems, SQL, Microservices, Python, C++", "Software Development Engineer", "1900000", "0-3", "Design and implement ultra high-throughput microservices, AWS cloud architectures, and e-commerce platform APIs.", "images/amazon.svg", "https://www.amazon.jobs"));
        list.add(new CompanyDef("Tata Consultancy Services", "Java, SQL, HTML, CSS, JavaScript, C++, DBMS, Python", "System Engineer", "500000", "0-2", "Support enterprise digital transformation, backend Java services, relational database management, and web portals.", "images/tcs.svg", "https://www.tcs.com/careers"));
        list.add(new CompanyDef("Infosys", "Java, Python, DBMS, SQL, HTML, JavaScript, Spring Boot, Angular", "Systems Engineer", "450000", "0-2", "Build reliable enterprise Java/Spring backends, responsive client UIs, and automated data processing services.", "images/infosys.svg", "https://www.infosys.com/careers.html"));
        list.add(new CompanyDef("Accenture", "Java, Spring Boot, SQL, Cloud, Angular, Full Stack, HTML, CSS", "Associate Software Engineer", "650000", "0-2", "Work on end-to-end full stack web applications, RESTful microservices, and modern cloud deployment environments.", "images/accenture.svg", "https://www.accenture.com/careers"));
        list.add(new CompanyDef("IBM", "Java, Python, Linux, Cloud, Docker, SQL, Kubernetes, AI", "Software Developer", "900000", "1-3", "Develop hybrid cloud solutions, containerized enterprise microservices, and AI-driven automation services.", "images/ibm.svg", "https://www.ibm.com/careers"));
        list.add(new CompanyDef("Oracle", "Java, SQL, PL/SQL, Database Internals, Cloud, OCI, Spring", "Applications Engineer", "1400000", "1-4", "Architect and maintain high-performance database management tools, cloud ERP modules, and mission-critical apps.", "images/oracle.svg", "https://www.oracle.com/corporate/careers/"));
        list.add(new CompanyDef("Meta", "Python, C++, Java, React, SQL, Distributed Systems, JavaScript", "Software Engineer", "2200000", "1-4", "Design large-scale distributed systems and real-time social infrastructure supporting billions of global users.", "images/meta.svg", "https://www.metacareers.com"));
        list.add(new CompanyDef("Apple", "Swift, Java, C++, Python, iOS, SQL, Machine Learning", "Software Engineer", "2000000", "1-4", "Create seamless hardware-software integration services, high-performance backends, and innovative application platforms.", "images/apple.svg", "https://www.apple.com/careers/"));
        list.add(new CompanyDef("JPMorgan Chase", "SQL, Python, Tableau, Excel, Power BI, Statistics, Data Modeling", "Data Analyst", "1100000", "0-2", "Extract business intelligence, build automated financial reporting pipelines, and analyze transaction patterns with SQL, Python, and Tableau.", "images/jpmorgan.svg", "https://careers.jpmorgan.com"));
        list.add(new CompanyDef("Spotify", "SQL, Python, BigQuery, Tableau, A/B Testing, Data Visualization, Excel", "Data Analyst", "1500000", "1-3", "Uncover user engagement insights, build self-service data pipelines, and design A/B experimentation models to drive streaming growth.", "images/spotify.svg", "https://www.lifeatspotify.com"));

        // 14-46: Specific requested companies
        list.add(new CompanyDef("Flipkart", "Java, Spring Boot, MySQL, Kafka, Redis, Distributed Systems", "Software Development Engineer I", "1700000", "1-3", "Architect high-throughput supply chain microservices, order tracking systems, and distributed e-commerce APIs.", "images/flipkart.svg", "https://www.flipkartcareers.com"));
        list.add(new CompanyDef("Myntra", "JavaScript, React, Node.js, HTML, CSS, Redux, Web Performance", "Frontend Engineer", "1400000", "1-3", "Build responsive fashion commerce user experiences, intuitive catalog browsing interfaces, and web rendering optimizations.", "images/myntra.svg", "https://careers.myntra.com"));
        list.add(new CompanyDef("Walmart", "Java, Spring, Cloud, SQL, Kafka, Microservices, Kubernetes", "Software Engineer II", "1800000", "1-4", "Develop retail infrastructure services, inventory replenishment microservices, and high-volume transaction systems.", "images/walmart.svg", "https://careers.walmart.com"));
        list.add(new CompanyDef("Wipro", "Java, SQL, Spring Boot, Python, HTML, CSS, JavaScript", "Project Engineer", "450000", "0-2", "Implement enterprise backend systems, system integration testing, and digital maintenance for global enterprise clients.", "images/wipro.svg", "https://careers.wipro.com"));
        list.add(new CompanyDef("HCLTech", "Java, C++, SQL, Cloud, Linux, Microservices, Python", "Software Engineer", "500000", "0-2", "Deliver enterprise application maintenance, IT infrastructure support, and scalable business logic microservices.", "images/hcltech.svg", "https://www.hcltech.com/careers"));
        list.add(new CompanyDef("Tech Mahindra", "Java, SQL, Microservices, Spring Boot, REST APIs, HTML", "Associate Software Engineer", "480000", "0-2", "Build telecom software backends, integration middleware, and enterprise cloud data pipelines.", "images/techmahindra.svg", "https://careers.techmahindra.com"));
        list.add(new CompanyDef("Cognizant", "Java, Spring Boot, SQL, React, Web Services, Cloud", "Programmer Analyst", "520000", "0-2", "Develop responsive web interfaces, modern API integrations, and cloud enterprise services for global clients.", "images/cognizant.svg", "https://careers.cognizant.com"));
        list.add(new CompanyDef("Capgemini", "Java, Spring Boot, Microservices, SQL, Docker, Angular", "Senior Software Engineer", "800000", "2-4", "Lead development on enterprise cloud transformation initiatives, RESTful API gateways, and distributed databases.", "images/capgemini.svg", "https://www.capgemini.com/careers"));
        list.add(new CompanyDef("EY", "Java, SQL, Power BI, Azure, Python, Spring Boot", "Technology Consultant", "850000", "1-3", "Provide technology risk analysis, cloud assurance solutions, and digital enterprise consulting for global organizations.", "images/ey.svg", "https://careers.ey.com"));
        list.add(new CompanyDef("KPMG", "SQL, Python, Tableau, Java, Data Analytics, Cloud", "Advisory Associate", "820000", "0-2", "Conduct data-driven financial advisory reviews, business intelligence reporting, and modern enterprise IT transformation.", "images/kpmg.svg", "https://kpmg.com/careers"));
        list.add(new CompanyDef("PwC", "Java, SQL, Cloud, Spring Boot, React, DevOps", "Technology Associate", "850000", "0-2", "Deliver digital engineering products, cloud architecture modernization, and financial technology platforms.", "images/pwc.svg", "https://www.pwc.com/careers"));
        list.add(new CompanyDef("LinkedIn", "Java, Python, Distributed Systems, Kafka, React, SQL", "Software Engineer", "2100000", "1-4", "Scale the professional graph, implement real-time recommendation engines, and power global member discovery.", "images/linkedin.svg", "https://careers.linkedin.com"));
        list.add(new CompanyDef("Adobe", "C++, Java, React, WebAssembly, Cloud, Python, Algorithms", "Member of Technical Staff", "1900000", "1-3", "Engineer world-class creative cloud services, document editing rendering engines, and high-performance digital media tools.", "images/adobe.svg", "https://www.adobe.com/careers.html"));
        list.add(new CompanyDef("Salesforce", "Java, Apex, React, Cloud, SQL, Distributed Systems", "Software Engineer", "1800000", "1-3", "Develop multi-tenant cloud CRM infrastructure, real-time messaging services, and extensible SaaS platform architectures.", "images/salesforce.svg", "https://careers.salesforce.com"));
        list.add(new CompanyDef("Cisco", "Python, C, C++, Networking, Linux, Java, Cloud", "Software Engineer", "1500000", "1-3", "Build next-generation network telemetry systems, cloud-managed edge switches, and high-reliability routing software.", "images/cisco.svg", "https://jobs.cisco.com"));
        list.add(new CompanyDef("SAP", "Java, ABAP, Cloud, Spring Boot, SQL, Kubernetes", "Developer Associate", "1300000", "0-3", "Implement scalable enterprise ERP cloud microservices, supply chain automation logic, and analytical database extensions.", "images/sap.svg", "https://jobs.sap.com"));
        list.add(new CompanyDef("PayPal", "Java, Spring Boot, SQL, REST APIs, Microservices, Kafka", "Software Engineer 1", "1600000", "1-3", "Design mission-critical payment authorization pipelines, digital wallet engines, and real-time fraud prevention systems.", "images/paypal.svg", "https://careers.pypl.com"));
        list.add(new CompanyDef("Visa", "Java, Distributed Systems, SQL, Cybersecurity, Microservices, C++", "Software Engineer", "1700000", "1-3", "Maintain global electronic funds transaction processing engines with sub-second latency and 99.999% reliability.", "images/visa.svg", "https://corporate.visa.com/careers.html"));
        list.add(new CompanyDef("Mastercard", "Java, Spring Boot, Cloud, SQL, API Design, Microservices", "Associate Software Engineer", "1400000", "0-2", "Build secure digital transaction APIs, cardholder verification services, and financial gateway infrastructure.", "images/mastercard.svg", "https://careers.mastercard.com"));
        list.add(new CompanyDef("Uber", "Go, Java, Python, Kafka, Distributed Systems, Redis", "Software Engineer I", "2200000", "1-3", "Optimize geospatial routing algorithms, driver dispatch systems, and real-time marketplace demand-pricing services.", "images/uber.svg", "https://www.uber.com/careers"));
        list.add(new CompanyDef("Ola", "Java, Python, Spring Boot, Microservices, MySQL, Redis", "Software Development Engineer", "1500000", "1-3", "Develop vehicle telematics systems, ride-hailing matchmaking logic, and high-concurrency mobility platforms.", "images/ola.svg", "https://www.olacabs.com/careers"));
        list.add(new CompanyDef("Swiggy", "Java, Go, Spring Boot, Microservices, Kafka, AWS", "Software Development Engineer I", "1600000", "1-3", "Power hyper-local delivery dispatch algorithms, live order tracking pipelines, and high-volume grocery catalog services.", "images/swiggy.svg", "https://careers.swiggy.com"));
        list.add(new CompanyDef("Zomato", "Node.js, Go, Python, React, MySQL, Redis, AWS", "Backend Engineer", "1500000", "1-3", "Build dining discovery engines, live restaurant ordering pipelines, and customer recommendation ranking microservices.", "images/zomato.svg", "https://www.zomato.com/careers"));
        list.add(new CompanyDef("PhonePe", "Java, Spring Boot, MySQL, Kafka, Redis, Microservices", "Software Engineer", "1800000", "1-3", "Engineer high-scale UPI payment processing systems, merchant settlement ledgers, and secure biometric payment flows.", "images/phonepe.svg", "https://www.phonepe.com/careers"));
        list.add(new CompanyDef("Razorpay", "PHP, Go, Python, MySQL, Kafka, AWS, Microservices", "Software Development Engineer", "1700000", "1-3", "Create merchant payment gateways, automated neo-banking payouts, and developer-friendly fintech checkout SDKs.", "images/razorpay.svg", "https://razorpay.com/jobs"));
        list.add(new CompanyDef("Freshworks", "Ruby, Java, JavaScript, AWS, MySQL, Redis", "Product Developer", "1300000", "1-3", "Develop customer service SaaS features, omnichannel messaging bots, and scalable CRM workflow automations.", "images/freshworks.svg", "https://www.freshworks.com/company/careers"));
        list.add(new CompanyDef("Zoho", "Java, C++, JavaScript, MySQL, Distributed Systems, Linux", "Software Developer", "900000", "0-3", "Architect in-house cloud office software, relational data layers, and secure email server backends.", "images/zoho.svg", "https://www.zoho.com/careers"));
        list.add(new CompanyDef("Qualcomm", "C, C++, Linux, Embedded Systems, Python, DSP, Firmware", "Engineer", "1500000", "0-3", "Program low-level 5G modem firmware, Snapdragon multimedia drivers, and embedded wireless protocols.", "images/qualcomm.svg", "https://www.qualcomm.com/company/careers"));
        list.add(new CompanyDef("Intel", "C++, Python, Linux, Computer Architecture, Data Structures", "Graduate Software Engineer", "1400000", "0-2", "Implement compiler toolchain enhancements, silicon validation harnesses, and multi-threaded CPU runtime libraries.", "images/intel.svg", "https://www.intel.com/jobs"));
        list.add(new CompanyDef("NVIDIA", "C++, CUDA, Python, Linux, GPU Programming, Deep Learning", "System Software Engineer", "2400000", "1-4", "Develop CUDA kernel drivers, AI acceleration libraries, and distributed GPU cluster communication runtimes.", "images/nvidia.svg", "https://www.nvidia.com/careers"));
        list.add(new CompanyDef("Samsung", "Java, C++, Android, Python, Embedded Systems, SQL", "Software Engineer", "1300000", "0-3", "Build camera image processing pipelines, One UI operating system services, and smart device firmware components.", "images/samsung.svg", "https://www.samsung.com/careers"));
        list.add(new CompanyDef("Dell Technologies", "Java, Python, Cloud, Spring Boot, SQL, Docker", "Software Engineer", "1200000", "1-3", "Engineer enterprise storage orchestration services, server management APIs, and automated backup software.", "images/dell.svg", "https://jobs.dell.com"));
        list.add(new CompanyDef("Goldman Sachs", "Java, Python, C++, SQL, Microservices, Spring", "Software Engineering Analyst", "1800000", "0-2", "Build ultra-low latency algorithmic trading microservices, financial risk modeling software, and market data feeds.", "images/goldmansachs.svg", "https://www.goldmansachs.com/careers"));
        list.add(new CompanyDef("Morgan Stanley", "Java, C++, Python, SQL, Cloud, Distributed Systems", "Technology Associate", "1700000", "1-3", "Develop institutional wealth management platforms, automated trade settlement workflows, and data pipelines.", "images/morganstanley.svg", "https://www.morganstanley.com/careers"));
        list.add(new CompanyDef("HSBC", "Java, Spring Boot, SQL, REST APIs, Cloud, Angular", "Trainee Software Engineer", "900000", "0-2", "Develop commercial banking web applications, international wire transfer services, and regulatory compliance engines.", "images/hsbc.svg", "https://www.hsbc.com/careers"));
        list.add(new CompanyDef("American Express", "Java, Spring Boot, Cloud, SQL, Kafka, React", "Engineer I", "1500000", "0-2", "Engineer cardholder rewards platforms, real-time transaction validation services, and customer credit decision engines.", "images/americanexpress.svg", "https://www.americanexpress.com/careers"));
        list.add(new CompanyDef("Barclays", "Java, Spring Boot, Oracle, Microservices, Cloud", "Graduate Developer", "1100000", "0-2", "Implement investment banking analytics, card settlement microservices, and modern open banking API solutions.", "images/barclays.svg", "https://search.jobs.barclays"));
        list.add(new CompanyDef("Walmart Global Tech", "Java, Spring Boot, Cloud, Kafka, SQL, Python", "Software Engineer", "1800000", "1-3", "Develop digital retail checkout microservices, cloud telemetry pipelines, and omni-channel customer fulfillment systems.", "images/walmartglobaltech.svg", "https://tech.walmart.com/careers"));
        list.add(new CompanyDef("Siemens", "Java, C++, Cloud, IoT, Python, Docker", "Software Engineer", "1100000", "1-3", "Develop industrial automation monitoring platforms, smart electrical grid services, and IoT device cloud integrations.", "images/siemens.svg", "https://jobs.siemens.com"));
        list.add(new CompanyDef("Bosch", "C++, Embedded C, Python, Linux, Java, IoT", "Associate Software Engineer", "900000", "0-2", "Build automotive telemetry ECU firmware, smart sensor interfaces, and connected mobility embedded controllers.", "images/bosch.svg", "https://www.bosch.com/careers"));
        list.add(new CompanyDef("Nokia", "C++, Java, Linux, Telecom, Networking, Python", "Software Engineer", "1100000", "1-3", "Program 5G base station network management software, telecom packet processing engines, and optical fiber control modules.", "images/nokia.svg", "https://www.nokia.com/about-us/careers"));
        list.add(new CompanyDef("Ericsson", "Java, Python, C++, Linux, Telecom Protocols, Cloud", "Integration Engineer", "950000", "0-2", "Deploy 5G core network packet routing software, virtualized telecom cloud nodes, and automated service orchestration.", "images/ericsson.svg", "https://www.ericsson.com/careers"));
        list.add(new CompanyDef("Airbnb", "Java, Kotlin, React, SQL, Distributed Systems, Cloud", "Backend Engineer", "2300000", "1-4", "Develop home booking engines, dynamic pricing recommendation systems, and international payment reconciliation ledgers.", "images/airbnb.svg", "https://careers.airbnb.com"));
        list.add(new CompanyDef("Booking.com", "Java, Perl, Python, MySQL, Redis, Cloud", "Software Developer", "1800000", "1-3", "Optimize global hotel reservation checkout funnels, inventory availability feeds, and localized customer experience.", "images/booking.svg", "https://careers.booking.com"));
        list.add(new CompanyDef("Dropbox", "Python, Go, Rust, Distributed Systems, SQL", "Software Engineer", "2100000", "1-3", "Architect multi-petabyte sync storage engines, file versioning trees, and encrypted collaborative cloud workspace backends.", "images/dropbox.svg", "https://www.dropbox.com/jobs"));
        list.add(new CompanyDef("GitHub", "Ruby, Go, TypeScript, Docker, Distributed Systems, SQL", "Systems Engineer", "2200000", "1-4", "Maintain Git core hosting infrastructure, GitHub Actions CI/CD runner clusters, and developer ecosystem platforms.", "images/github.svg", "https://github.com/about/careers"));

        // 60-110: Additional Top Tech, BFSI, Consulting, Startups
        list.add(new CompanyDef("Netflix", "Java, Node.js, AWS, Distributed Systems, Microservices, Python", "Senior Software Engineer", "2600000", "2-5", "Scale global video streaming edge caches, encoding pipelines, and content recommendation algorithms.", "images/netflix.svg", "https://jobs.netflix.com"));
        list.add(new CompanyDef("Twitter (X)", "Scala, Java, Python, Distributed Systems, GraphQL, Kafka", "Platform Engineer", "2200000", "1-4", "Develop high-throughput real-time timeline ingestion, notification delivery queues, and creator subscription tools.", "images/twitter.svg", "https://careers.x.com"));
        list.add(new CompanyDef("Pinterest", "Python, Java, React, SQL, Cloud, Machine Learning", "Full Stack Engineer", "1800000", "1-3", "Build visual discovery feeds, shopping graph indexing, and responsive web user interfaces.", "images/pinterest.svg", "https://www.pinterestcareers.com"));
        list.add(new CompanyDef("Reddit", "Python, Go, React, PostgreSQL, Redis, Kubernetes", "Backend Developer", "1900000", "1-3", "Develop community discussion engines, voting tally microservices, and high-traffic comment tree storage.", "images/reddit.svg", "https://www.redditinc.com/careers"));
        list.add(new CompanyDef("eBay", "Java, Spring Boot, Oracle, Elasticsearch, Cloud", "Software Engineer", "1500000", "1-3", "Power global marketplace product search indexing, seller listing APIs, and bidding transaction microservices.", "images/ebay.svg", "https://careers.ebayinc.com"));
        list.add(new CompanyDef("Stripe", "Ruby, Go, Java, Distributed Systems, SQL, Cloud", "Infrastructure Engineer", "2400000", "2-4", "Build the financial infrastructure of the internet: developer APIs, payout processing, and fraud detection.", "images/stripe.svg", "https://stripe.com/jobs"));
        list.add(new CompanyDef("Atlassian", "Java, Kotlin, React, AWS, Microservices, TypeScript", "Software Engineer", "1900000", "1-3", "Engineer collaborative developer tools like Jira and Confluence, supporting millions of daily agile sprints.", "images/atlassian.svg", "https://www.atlassian.com/company/careers"));
        list.add(new CompanyDef("ServiceNow", "Java, JavaScript, SQL, Linux, Cloud Infrastructure", "Associate Software Developer", "1400000", "0-2", "Create enterprise digital workflow engines, incident management automations, and scalable cloud tables.", "images/servicenow.svg", "https://careers.servicenow.com"));
        list.add(new CompanyDef("VMware", "Go, Python, C++, Java, Kubernetes, Linux", "Cloud Software Engineer", "1700000", "1-4", "Develop hypervisor virtualization layers, software-defined networking bridges, and cloud container orchestration.", "images/vmware.svg", "https://careers.vmware.com"));
        list.add(new CompanyDef("Intuit", "Java, Spring Boot, React, AWS, GraphQL, SQL", "Software Engineer 1", "1600000", "0-2", "Power TurboTax and QuickBooks personal finance calculations, tax filing automations, and small business dashboards.", "images/intuit.svg", "https://www.intuit.com/careers"));
        list.add(new CompanyDef("Workday", "Java, Scala, Cloud, SQL, Distributed Systems", "Software Development Engineer", "1500000", "1-3", "Develop enterprise human capital cloud databases, payroll accounting pipelines, and enterprise planning services.", "images/workday.svg", "https://www.workday.com/en-us/company/careers"));
        list.add(new CompanyDef("Snowflake", "C++, Java, SQL, Cloud, Distributed Systems, Python", "Cloud Database Engineer", "2500000", "2-5", "Engineer elastic compute query execution kernels, metadata catalog storage, and multi-cloud data warehousing.", "images/snowflake.svg", "https://careers.snowflake.com"));
        list.add(new CompanyDef("Databricks", "Scala, Java, Python, Spark, Cloud, Kubernetes", "Platform Engineer", "2500000", "2-5", "Develop lakehouse unified data analytics platforms, Apache Spark runtimes, and machine learning model serving.", "images/databricks.svg", "https://www.databricks.com/company/careers"));
        list.add(new CompanyDef("Palantir", "Java, TypeScript, Python, Distributed Systems, Docker", "Forward Deployed Software Engineer", "2100000", "1-4", "Implement Gotham and Foundry enterprise intelligence suites, integrating heterogeneous data for critical institutions.", "images/palantir.svg", "https://www.palantir.com/careers"));
        list.add(new CompanyDef("Twilio", "Java, Python, C++, Distributed Systems, AWS, Kafka", "Core API Engineer", "1700000", "1-3", "Develop cloud communication REST APIs powering billions of SMS notifications, voice calls, and authentication OTPs.", "images/twilio.svg", "https://www.twilio.com/company/jobs"));
        list.add(new CompanyDef("Paytm", "Java, Spring Boot, MySQL, Kafka, Redis, Microservices", "Associate SDE", "1200000", "0-2", "Build merchant QR payment infrastructure, bill payment aggregations, and high-volume wallet ledgers.", "images/paytm.svg", "https://jobs.lever.co/paytm"));
        list.add(new CompanyDef("Meesho", "Java, Spring Boot, MySQL, Kafka, Redis, AWS", "SDE 1", "1500000", "0-2", "Architect social e-commerce reseller tools, supplier order processing systems, and logistics route tracking.", "images/meesho.svg", "https://www.meesho.io/careers"));
        list.add(new CompanyDef("Zepto", "Go, Python, Postgres, Redis, Microservices, AWS", "Backend Engineer", "1600000", "1-3", "Design 10-minute quick-commerce warehouse routing algorithms, dark store inventory reservation, and rider tracking.", "images/zepto.svg", "https://www.zeptonow.com/careers"));
        list.add(new CompanyDef("CRED", "Go, Java, Kotlin, React Native, Postgres, Kafka", "Backend Developer", "2000000", "1-4", "Develop gamified reward engines, instant credit card bill pay settlement, and merchant rewards checkout platforms.", "images/cred.svg", "https://cred.club/careers"));
        list.add(new CompanyDef("Groww", "Java, Spring Boot, MySQL, Kafka, Redis, Microservices", "Software Development Engineer", "1600000", "1-3", "Build stock brokerage trade order execution pipelines, mutual fund investment APIs, and portfolio performance analyzers.", "images/groww.svg", "https://groww.in/careers"));
        list.add(new CompanyDef("Zerodha", "Go, Python, PostgreSQL, Redis, Vue.js, Linux", "Systems Developer", "1700000", "1-3", "Engineer Kite trading platform order routing microservices with sub-millisecond dispatch to stock exchanges.", "images/zerodha.svg", "https://zerodha.com/careers"));
        list.add(new CompanyDef("Nykaa", "Java, Node.js, React, MySQL, MongoDB, AWS", "Full Stack Developer", "1300000", "1-3", "Develop beauty e-commerce storefronts, personal beauty advisor recommendation APIs, and checkout funnels.", "images/nykaa.svg", "https://www.nykaa.com/careers"));
        list.add(new CompanyDef("BigBasket", "Python, Django, Java, PostgreSQL, Redis, Docker", "Software Engineer", "1300000", "1-3", "Build automated grocery warehouse picking algorithms, cold chain supply chain ledgers, and scheduled delivery APIs.", "images/bigbasket.svg", "https://careers.bigbasket.com"));
        list.add(new CompanyDef("Urban Company", "Node.js, React, Python, MongoDB, Redis, AWS", "SDE 1", "1400000", "0-2", "Engineer home services partner scheduling systems, real-time demand dispatching, and quality verification workflows.", "images/urbancompany.svg", "https://www.urbancompany.com/careers"));
        list.add(new CompanyDef("InMobi", "Java, Spark, Kafka, Hadoop, Cloud, Big Data", "Software Development Engineer", "1600000", "1-3", "Build ad-tech bidding engines processing hundreds of thousands of ad auction requests per second globally.", "images/inmobi.svg", "https://www.inmobi.com/company/careers"));
        list.add(new CompanyDef("AMD", "C++, Python, Linux, GPU Drivers, Computer Architecture", "Software Design Engineer", "1600000", "1-3", "Develop ROCm open software GPU compute runtimes, Radeon graphics driver pipelines, and processor microcode utilities.", "images/amd.svg", "https://www.amd.com/en/corporate/careers"));
        list.add(new CompanyDef("Texas Instruments", "C, C++, Embedded Systems, RTOS, Python, DSP", "Applications Engineer", "1300000", "0-2", "Develop analog and embedded microcontroller firmware libraries, DSP signal filtering, and customer reference designs.", "images/ti.svg", "https://careers.ti.com"));
        list.add(new CompanyDef("Broadcom", "C, C++, Linux, Networking, Device Drivers, Kernel", "Firmware Engineer", "1600000", "1-4", "Program enterprise switch silicon microcode, Ethernet controller hardware drivers, and PCIe fiber fabric software.", "images/broadcom.svg", "https://www.broadcom.com/company/careers"));
        list.add(new CompanyDef("Micron Technology", "C++, Python, Linux, Data Structures, Storage Systems", "Software Engineer", "1300000", "0-2", "Implement NAND flash memory controller firmware, SSD performance benchmarking, and DRAM testing automation.", "images/micron.svg", "https://www.micron.com/careers"));
        list.add(new CompanyDef("ARM", "C, C++, Python, Computer Architecture, Compilers, Linux", "Graduate Software Engineer", "1400000", "0-2", "Validate ARM instruction set architecture emulators, optimize LLVM compiler backends, and develop system models.", "images/arm.svg", "https://www.arm.com/company/careers"));
        list.add(new CompanyDef("Western Digital", "C, C++, Embedded Systems, Linux, Python, Storage", "Firmware Software Engineer", "1200000", "1-3", "Develop NVMe flash memory firmware, wear-leveling controllers, and high-performance hard drive data channels.", "images/westerndigital.svg", "https://www.westerndigital.com/company/careers"));
        list.add(new CompanyDef("Sony", "C++, Python, Linux, Computer Vision, Graphics, Java", "Software Engineer", "1400000", "1-3", "Build PlayStation network microservices, image sensor SDKs, and multimedia processing pipelines.", "images/sony.svg", "https://www.sony.com/careers"));
        list.add(new CompanyDef("Lenovo", "Java, Python, Cloud, Linux, C++, React", "Cloud Solution Developer", "1100000", "1-3", "Develop cloud enterprise device telemetry software, server fleet deployment automation, and modern web portals.", "images/lenovo.svg", "https://www.lenovo.com/careers"));
        list.add(new CompanyDef("HP", "Java, C++, Python, Cloud, Microservices, React", "Software Engineer", "1200000", "1-3", "Develop cloud-connected printing microservices, endpoint security utilities, and device telemetry software.", "images/hp.svg", "https://jobs.hp.com"));
        list.add(new CompanyDef("LTIMindtree", "Java, Spring Boot, Cloud, Microservices, SQL, Angular", "Senior Specialist", "750000", "1-3", "Work on modern cloud migrations, enterprise banking interfaces, and scalable REST API architectures.", "images/ltimindtree.svg", "https://www.ltimindtree.com/careers"));
        list.add(new CompanyDef("Hexaware", "Java, Spring Boot, SQL, Cloud, Automation, React", "Software Engineer", "550000", "0-2", "Deliver enterprise application development, test automation suites, and cloud platform integrations.", "images/hexaware.svg", "https://hexaware.com/careers"));
        list.add(new CompanyDef("Mphasis", "Java, Spring Boot, AWS, SQL, Microservices, Python", "Associate Software Engineer", "500000", "0-2", "Support digital banking platform modernisation, cloud application support, and database operations.", "images/mphasis.svg", "https://careers.mphasis.com"));
        list.add(new CompanyDef("Persistent Systems", "Java, Spring Boot, Cloud, Docker, React, Microservices", "Lead Software Engineer", "1200000", "2-4", "Architect healthcare and fintech software applications, containerized services, and secure API gateways.", "images/persistentsystems.svg", "https://www.persistent.com/careers"));
        list.add(new CompanyDef("DXC Technology", "Java, SQL, Python, Cloud, Linux, Web Services", "Associate Professional Developer", "480000", "0-2", "Develop and deploy enterprise IT service automation, legacy software migrations, and cloud hosting solutions.", "images/dxctechnology.svg", "https://dxc.com/careers"));
        list.add(new CompanyDef("Citi", "Java, Spring Boot, Oracle, Microservices, Cloud, Angular", "Technology Analyst", "1200000", "0-2", "Develop global institutional trading portals, cross-border payments routing, and financial compliance services.", "images/citi.svg", "https://careers.citigroup.com"));
        list.add(new CompanyDef("Bank of America", "Java, Python, SQL, Cloud, Spring Boot, React", "Senior Tech Associate", "1300000", "1-3", "Build consumer digital banking applications, automated fraud alerts, and scalable transaction ledgers.", "images/bankofamerica.svg", "https://careers.bankofamerica.com"));
        list.add(new CompanyDef("Wells Fargo", "Java, Spring, Oracle, Microservices, Cloud, Python", "Software Engineer", "1250000", "1-3", "Develop mortgage application platforms, commercial lending analytics, and real-time account verification APIs.", "images/wellsfargo.svg", "https://www.wellsfargojobs.com"));
        list.add(new CompanyDef("Standard Chartered", "Java, Spring Boot, SQL, Cloud, Microservices, Kafka", "Developer Analyst", "1150000", "0-2", "Build international wealth management tools, foreign exchange settlement services, and secure mobile banking APIs.", "images/standardchartered.svg", "https://www.sc.com/careers"));
        list.add(new CompanyDef("Deutsche Bank", "Java, Python, SQL, Cloud, Microservices, Oracle", "Graduate Developer", "1200000", "0-2", "Implement corporate banking ledgers, risk analytics computational engines, and regulatory trade reporting.", "images/deutschebank.svg", "https://careers.db.com"));
        list.add(new CompanyDef("BNP Paribas", "Java, Spring Boot, SQL, Angular, Cloud, Microservices", "Software Developer", "1100000", "0-2", "Develop asset management web platforms, automated market compliance checks, and transaction auditing pipelines.", "images/bnpparibas.svg", "https://group.bnpparibas/en/careers"));
        list.add(new CompanyDef("McKinsey & Company", "Python, SQL, Java, React, Cloud, Data Science", "Digital Specialist", "1600000", "1-3", "Develop customized analytics platforms, client digital transformation prototypes, and machine learning models.", "images/mckinsey.svg", "https://www.mckinsey.com/careers"));
        list.add(new CompanyDef("Boston Consulting Group", "Python, SQL, Tableau, Cloud, Java, Data Analytics", "Technology Associate", "1500000", "1-3", "Build big-data scenario simulation engines, corporate performance dashboards, and tech strategy models.", "images/bcg.svg", "https://careers.bcg.com"));
        list.add(new CompanyDef("Bain & Company", "Python, SQL, Cloud, Machine Learning, Power BI, Java", "Expert Engineer", "1550000", "1-3", "Build analytics infrastructure, market sizing predictive engines, and operational efficiency tools for enterprise clients.", "images/bain.svg", "https://www.bain.com/careers"));
        list.add(new CompanyDef("Honeywell", "C++, Java, Embedded Systems, IoT, Cloud, Python", "Software Engineer", "1100000", "1-3", "Build avionics telemetry services, smart building automation controllers, and industrial IoT monitoring platforms.", "images/honeywell.svg", "https://careers.honeywell.com"));
        list.add(new CompanyDef("Schneider Electric", "Java, C++, Cloud, IoT, Spring Boot, Docker", "Cloud Platform Engineer", "1150000", "1-3", "Develop EcoStruxure IoT cloud energy management software, smart power distribution monitoring, and microservices.", "images/schneiderelectric.svg", "https://www.se.com/careers"));
        list.add(new CompanyDef("Philips", "Java, C++, Python, Healthcare Tech, Cloud, SQL", "Software Development Engineer", "1200000", "1-3", "Develop hospital patient monitoring software, medical imaging algorithms, and secure telehealth cloud infrastructure.", "images/philips.svg", "https://www.careers.philips.com"));

        return list;
    }

    public static void seedOrUpdateCompanies(Connection conn) {
        if (conn == null) return;
        try {
            List<CompanyDef> companies = getFullCompanyList();
            String checkSql = "SELECT id, logo FROM companies WHERE LOWER(company_name) = ?";
            String insertSql = "INSERT INTO companies (company_name, skills, role, salary, experience, description, logo, apply_url, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String updateSql = "UPDATE companies SET skills = ?, role = ?, salary = ?, experience = ?, description = ?, logo = ?, apply_url = ?, status = ? WHERE id = ?";

            // Also ensure Capgemini and Cisco logos are set if they were previously default
            try (PreparedStatement psUpdate = conn.prepareStatement(
                    "UPDATE companies SET logo = ? WHERE LOWER(company_name) = ? AND logo LIKE '%default%'")) {
                psUpdate.setString(1, "images/capgemini.svg");
                psUpdate.setString(2, "capgemini");
                psUpdate.executeUpdate();

                psUpdate.setString(1, "images/cisco.svg");
                psUpdate.setString(2, "cisco");
                psUpdate.executeUpdate();
            }

            for (CompanyDef c : companies) {
                try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                    psCheck.setString(1, c.name.toLowerCase().trim());
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next()) {
                            int id = rs.getInt("id");
                            String currentLogo = rs.getString("logo");
                            if (currentLogo == null || currentLogo.contains("default-company.svg")) {
                                try (PreparedStatement psUp = conn.prepareStatement("UPDATE companies SET logo = ? WHERE id = ?")) {
                                    psUp.setString(1, c.logo);
                                    psUp.setInt(2, id);
                                    psUp.executeUpdate();
                                }
                            }
                        } else {
                            try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                                psIns.setString(1, c.name);
                                psIns.setString(2, c.skills);
                                psIns.setString(3, c.role);
                                psIns.setString(4, c.salary);
                                psIns.setString(5, c.exp);
                                psIns.setString(6, c.desc);
                                psIns.setString(7, c.logo);
                                psIns.setString(8, c.url);
                                psIns.setString(9, c.status);
                                psIns.executeUpdate();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[CompanySeedData] Error during seed/update: " + e.getMessage());
        }
    }
}
