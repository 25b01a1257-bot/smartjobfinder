package com.SmartJobFinder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;

public class LogoHelper {

    public static final String DEFAULT_LOGO = "images/default-company.svg";

    private static final Map<String, String> COMPANY_TO_LOGO_MAP = new HashMap<>();

    static {
        // Map common company names to their exact SVG filenames
        COMPANY_TO_LOGO_MAP.put("google", "google.svg");
        COMPANY_TO_LOGO_MAP.put("deloitte", "deloitte.svg");
        COMPANY_TO_LOGO_MAP.put("microsoft", "microsoft.svg");
        COMPANY_TO_LOGO_MAP.put("amazon", "amazon.svg");
        COMPANY_TO_LOGO_MAP.put("tcs", "tcs.svg");
        COMPANY_TO_LOGO_MAP.put("tataconsultancyservices", "tcs.svg");
        COMPANY_TO_LOGO_MAP.put("infosys", "infosys.svg");
        COMPANY_TO_LOGO_MAP.put("accenture", "accenture.svg");
        COMPANY_TO_LOGO_MAP.put("ibm", "ibm.svg");
        COMPANY_TO_LOGO_MAP.put("oracle", "oracle.svg");
        COMPANY_TO_LOGO_MAP.put("meta", "meta.svg");
        COMPANY_TO_LOGO_MAP.put("facebook", "meta.svg");
        COMPANY_TO_LOGO_MAP.put("apple", "apple.svg");
        COMPANY_TO_LOGO_MAP.put("jpmorgan", "jpmorgan.svg");
        COMPANY_TO_LOGO_MAP.put("jpmorganchase", "jpmorgan.svg");
        COMPANY_TO_LOGO_MAP.put("spotify", "spotify.svg");
        COMPANY_TO_LOGO_MAP.put("flipkart", "flipkart.svg");
        COMPANY_TO_LOGO_MAP.put("myntra", "myntra.svg");
        COMPANY_TO_LOGO_MAP.put("walmart", "walmart.svg");
        COMPANY_TO_LOGO_MAP.put("wipro", "wipro.svg");
        COMPANY_TO_LOGO_MAP.put("hcltech", "hcltech.svg");
        COMPANY_TO_LOGO_MAP.put("hcl", "hcltech.svg");
        COMPANY_TO_LOGO_MAP.put("techmahindra", "techmahindra.svg");
        COMPANY_TO_LOGO_MAP.put("cognizant", "cognizant.svg");
        COMPANY_TO_LOGO_MAP.put("capgemini", "capgemini.svg");
        COMPANY_TO_LOGO_MAP.put("ey", "ey.svg");
        COMPANY_TO_LOGO_MAP.put("ernstandyoung", "ey.svg");
        COMPANY_TO_LOGO_MAP.put("kpmg", "kpmg.svg");
        COMPANY_TO_LOGO_MAP.put("pwc", "pwc.svg");
        COMPANY_TO_LOGO_MAP.put("pricewaterhousecoopers", "pwc.svg");
        COMPANY_TO_LOGO_MAP.put("linkedin", "linkedin.svg");
        COMPANY_TO_LOGO_MAP.put("adobe", "adobe.svg");
        COMPANY_TO_LOGO_MAP.put("salesforce", "salesforce.svg");
        COMPANY_TO_LOGO_MAP.put("cisco", "cisco.svg");
        COMPANY_TO_LOGO_MAP.put("sap", "sap.svg");
        COMPANY_TO_LOGO_MAP.put("paypal", "paypal.svg");
        COMPANY_TO_LOGO_MAP.put("visa", "visa.svg");
        COMPANY_TO_LOGO_MAP.put("mastercard", "mastercard.svg");
        COMPANY_TO_LOGO_MAP.put("uber", "uber.svg");
        COMPANY_TO_LOGO_MAP.put("ola", "ola.svg");
        COMPANY_TO_LOGO_MAP.put("swiggy", "swiggy.svg");
        COMPANY_TO_LOGO_MAP.put("zomato", "zomato.svg");
        COMPANY_TO_LOGO_MAP.put("phonepe", "phonepe.svg");
        COMPANY_TO_LOGO_MAP.put("razorpay", "razorpay.svg");
        COMPANY_TO_LOGO_MAP.put("freshworks", "freshworks.svg");
        COMPANY_TO_LOGO_MAP.put("zoho", "zoho.svg");
        COMPANY_TO_LOGO_MAP.put("qualcomm", "qualcomm.svg");
        COMPANY_TO_LOGO_MAP.put("intel", "intel.svg");
        COMPANY_TO_LOGO_MAP.put("nvidia", "nvidia.svg");
        COMPANY_TO_LOGO_MAP.put("samsung", "samsung.svg");
        COMPANY_TO_LOGO_MAP.put("dell", "dell.svg");
        COMPANY_TO_LOGO_MAP.put("delltechnologies", "dell.svg");
        COMPANY_TO_LOGO_MAP.put("goldmansachs", "goldmansachs.svg");
        COMPANY_TO_LOGO_MAP.put("morganstanley", "morganstanley.svg");
        COMPANY_TO_LOGO_MAP.put("hsbc", "hsbc.svg");
        COMPANY_TO_LOGO_MAP.put("americanexpress", "americanexpress.svg");
        COMPANY_TO_LOGO_MAP.put("amex", "americanexpress.svg");
        COMPANY_TO_LOGO_MAP.put("barclays", "barclays.svg");
        COMPANY_TO_LOGO_MAP.put("walmartglobaltech", "walmartglobaltech.svg");
        COMPANY_TO_LOGO_MAP.put("siemens", "siemens.svg");
        COMPANY_TO_LOGO_MAP.put("bosch", "bosch.svg");
        COMPANY_TO_LOGO_MAP.put("nokia", "nokia.svg");
        COMPANY_TO_LOGO_MAP.put("ericsson", "ericsson.svg");
        COMPANY_TO_LOGO_MAP.put("airbnb", "airbnb.svg");
        COMPANY_TO_LOGO_MAP.put("booking", "booking.svg");
        COMPANY_TO_LOGO_MAP.put("bookingcom", "booking.svg");
        COMPANY_TO_LOGO_MAP.put("dropbox", "dropbox.svg");
        COMPANY_TO_LOGO_MAP.put("github", "github.svg");
        COMPANY_TO_LOGO_MAP.put("netflix", "netflix.svg");
        COMPANY_TO_LOGO_MAP.put("twitter", "twitter.svg");
        COMPANY_TO_LOGO_MAP.put("x", "twitter.svg");
        COMPANY_TO_LOGO_MAP.put("pinterest", "pinterest.svg");
        COMPANY_TO_LOGO_MAP.put("reddit", "reddit.svg");
        COMPANY_TO_LOGO_MAP.put("ebay", "ebay.svg");
        COMPANY_TO_LOGO_MAP.put("stripe", "stripe.svg");
        COMPANY_TO_LOGO_MAP.put("atlassian", "atlassian.svg");
        COMPANY_TO_LOGO_MAP.put("servicenow", "servicenow.svg");
        COMPANY_TO_LOGO_MAP.put("vmware", "vmware.svg");
        COMPANY_TO_LOGO_MAP.put("intuit", "intuit.svg");
        COMPANY_TO_LOGO_MAP.put("workday", "workday.svg");
        COMPANY_TO_LOGO_MAP.put("snowflake", "snowflake.svg");
        COMPANY_TO_LOGO_MAP.put("databricks", "databricks.svg");
        COMPANY_TO_LOGO_MAP.put("palantir", "palantir.svg");
        COMPANY_TO_LOGO_MAP.put("twilio", "twilio.svg");
        COMPANY_TO_LOGO_MAP.put("paytm", "paytm.svg");
        COMPANY_TO_LOGO_MAP.put("meesho", "meesho.svg");
        COMPANY_TO_LOGO_MAP.put("zepto", "zepto.svg");
        COMPANY_TO_LOGO_MAP.put("cred", "cred.svg");
        COMPANY_TO_LOGO_MAP.put("groww", "groww.svg");
        COMPANY_TO_LOGO_MAP.put("zerodha", "zerodha.svg");
        COMPANY_TO_LOGO_MAP.put("nykaa", "nykaa.svg");
        COMPANY_TO_LOGO_MAP.put("bigbasket", "bigbasket.svg");
        COMPANY_TO_LOGO_MAP.put("urbancompany", "urbancompany.svg");
        COMPANY_TO_LOGO_MAP.put("inmobi", "inmobi.svg");
        COMPANY_TO_LOGO_MAP.put("amd", "amd.svg");
        COMPANY_TO_LOGO_MAP.put("ti", "ti.svg");
        COMPANY_TO_LOGO_MAP.put("texasinstruments", "ti.svg");
        COMPANY_TO_LOGO_MAP.put("broadcom", "broadcom.svg");
        COMPANY_TO_LOGO_MAP.put("micron", "micron.svg");
        COMPANY_TO_LOGO_MAP.put("microntechnology", "micron.svg");
        COMPANY_TO_LOGO_MAP.put("arm", "arm.svg");
        COMPANY_TO_LOGO_MAP.put("westerndigital", "westerndigital.svg");
        COMPANY_TO_LOGO_MAP.put("wd", "westerndigital.svg");
        COMPANY_TO_LOGO_MAP.put("sony", "sony.svg");
        COMPANY_TO_LOGO_MAP.put("lenovo", "lenovo.svg");
        COMPANY_TO_LOGO_MAP.put("hp", "hp.svg");
        COMPANY_TO_LOGO_MAP.put("ltimindtree", "ltimindtree.svg");
        COMPANY_TO_LOGO_MAP.put("hexaware", "hexaware.svg");
        COMPANY_TO_LOGO_MAP.put("mphasis", "mphasis.svg");
        COMPANY_TO_LOGO_MAP.put("persistentsystems", "persistentsystems.svg");
        COMPANY_TO_LOGO_MAP.put("dxctechnology", "dxctechnology.svg");
        COMPANY_TO_LOGO_MAP.put("citi", "citi.svg");
        COMPANY_TO_LOGO_MAP.put("citigroup", "citi.svg");
        COMPANY_TO_MAP("bankofamerica", "bankofamerica.svg");
        COMPANY_TO_MAP("bofa", "bankofamerica.svg");
        COMPANY_TO_MAP("wellsfargo", "wellsfargo.svg");
        COMPANY_TO_MAP("standardchartered", "standardchartered.svg");
        COMPANY_TO_MAP("deutschebank", "deutschebank.svg");
        COMPANY_TO_MAP("bnpparibas", "bnpparibas.svg");
        COMPANY_TO_MAP("mckinsey", "mckinsey.svg");
        COMPANY_TO_MAP("mckinseyandcompany", "mckinsey.svg");
        COMPANY_TO_MAP("bcg", "bcg.svg");
        COMPANY_TO_MAP("bostonconsultinggroup", "bcg.svg");
        COMPANY_TO_MAP("bain", "bain.svg");
        COMPANY_TO_MAP("bainandcompany", "bain.svg");
        COMPANY_TO_MAP("honeywell", "honeywell.svg");
        COMPANY_TO_MAP("schneiderelectric", "schneiderelectric.svg");
        COMPANY_TO_MAP("philips", "philips.svg");
    }

    private static void COMPANY_TO_MAP(String key, String val) {
        COMPANY_TO_LOGO_MAP.put(key, val);
    }

    /**
     * Handles file upload from admin form Part 'logo_file'.
     * Saves the uploaded file into web/images/ and deployed directory.
     * Returns "images/<filename>" or null if no valid file uploaded.
     */
    public static String handleLogoUpload(Part filePart, String companyName, ServletContext context) {
        if (filePart == null || filePart.getSize() <= 0) {
            return null;
        }

        String submittedName = filePart.getSubmittedFileName();
        if (submittedName == null || submittedName.trim().isEmpty()) {
            return null;
        }

        String ext = "";
        int dotIdx = submittedName.lastIndexOf('.');
        if (dotIdx >= 0) {
            ext = submittedName.substring(dotIdx).toLowerCase();
        }

        List<String> allowedExts = Arrays.asList(".svg", ".png", ".jpg", ".jpeg", ".webp");
        if (!allowedExts.contains(ext)) {
            ext = ".png"; // safe fallback extension
        }

        String baseSlug = (companyName != null && !companyName.trim().isEmpty())
                ? companyName.toLowerCase().replaceAll("[^a-z0-9]", "")
                : "uploaded_logo_" + System.currentTimeMillis();
        String savedFileName = baseSlug + ext;

        // Save to multiple targets: deployed webapp images, workspace web/images, workspace web/image
        List<File> targetDirs = new ArrayList<>();

        if (context != null) {
            String deployedImagesPath = context.getRealPath("/images");
            if (deployedImagesPath != null) {
                targetDirs.add(new File(deployedImagesPath));
            }
            String deployedImagePath = context.getRealPath("/image");
            if (deployedImagePath != null) {
                targetDirs.add(new File(deployedImagePath));
            }
        }

        // Workspace project paths
        targetDirs.add(new File("web/images"));
        targetDirs.add(new File("web/image"));
        targetDirs.add(new File("image"));

        try {
            byte[] bytes;
            try (InputStream in = filePart.getInputStream()) {
                bytes = in.readAllBytes();
            }

            for (File dir : targetDirs) {
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File dest = new File(dir, savedFileName);
                try (OutputStream out = new FileOutputStream(dest)) {
                    out.write(bytes);
                } catch (Exception ignore) {}
            }

            return "images/" + savedFileName;
        } catch (Exception e) {
            System.err.println("[LogoHelper] Error saving uploaded logo: " + e.getMessage());
            return null;
        }
    }

    /**
     * Resolves logo path based on uploaded file, selected path, and company name.
     */
    public static String resolveLogo(Part filePart, String rawLogo, String companyName, ServletContext context) {
        // 1. Try uploaded file first
        String uploadedPath = handleLogoUpload(filePart, companyName, context);
        if (uploadedPath != null) {
            return uploadedPath;
        }

        // 2. If rawLogo was explicitly entered/selected
        if (rawLogo != null) {
            String clean = rawLogo.trim();
            if (!clean.isEmpty() && !clean.equalsIgnoreCase(DEFAULT_LOGO)) {
                // If user entered only "flipkart.svg", normalize to "images/flipkart.svg"
                if (!clean.startsWith("images/") && !clean.startsWith("image/") && !clean.startsWith("http://") && !clean.startsWith("https://")) {
                    clean = "images/" + clean;
                }
                return clean;
            }
        }

        // 3. Smart auto-detect from company name
        if (companyName != null && !companyName.trim().isEmpty()) {
            String slug = companyName.toLowerCase().replaceAll("[^a-z0-9]", "");
            if (COMPANY_TO_LOGO_MAP.containsKey(slug)) {
                return "images/" + COMPANY_TO_LOGO_MAP.get(slug);
            }
            // Check individual words
            String[] words = companyName.toLowerCase().split("[^a-z0-9]+");
            for (String w : words) {
                if (w.length() >= 2 && COMPANY_TO_LOGO_MAP.containsKey(w)) {
                    return "images/" + COMPANY_TO_LOGO_MAP.get(w);
                }
            }
            // Check prefix/contains only for keys of 4 or more characters
            for (Map.Entry<String, String> entry : COMPANY_TO_LOGO_MAP.entrySet()) {
                String k = entry.getKey();
                if (k.length() >= 4 && slug.contains(k)) {
                    return "images/" + entry.getValue();
                }
            }
        }

        // 4. Default fallback
        return DEFAULT_LOGO;
    }

    /**
     * Returns a sorted list of all available preset logos for admin dropdowns.
     */
    public static List<String> getPresetLogos() {
        List<String> list = new ArrayList<>();
        list.add(DEFAULT_LOGO);
        for (String file : COMPANY_TO_LOGO_MAP.values()) {
            String p = "images/" + file;
            if (!list.contains(p)) {
                list.add(p);
            }
        }
        Collections.sort(list);
        return list;
    }

    public static String getDisplayNameForLogo(String path) {
        if (path == null || path.isEmpty()) return "Default Building Icon";
        String fn = path.substring(path.lastIndexOf('/') + 1).replace(".svg", "").replace(".png", "");
        if (fn.equals("default-company")) return "🏢 Default Company Icon";
        if (fn.equals("tcs")) return "Tata Consultancy Services (TCS)";
        if (fn.equals("ey")) return "Ernst & Young (EY)";
        if (fn.equals("pwc")) return "PwC (PricewaterhouseCoopers)";
        if (fn.equals("kpmg")) return "KPMG";
        if (fn.equals("ibm")) return "IBM";
        if (fn.equals("sap")) return "SAP";
        if (fn.equals("hp")) return "HP";
        if (fn.equals("ti")) return "Texas Instruments (TI)";
        if (fn.equals("amd")) return "AMD";
        if (fn.equals("arm")) return "ARM";
        if (fn.equals("wd") || fn.equals("westerndigital")) return "Western Digital";
        if (fn.equals("ltimindtree")) return "LTIMindtree";
        if (fn.equals("hcltech")) return "HCLTech";
        if (fn.equals("techmahindra")) return "Tech Mahindra";
        if (fn.equals("walmartglobaltech")) return "Walmart Global Tech";
        if (fn.equals("americanexpress")) return "American Express (AMEX)";
        if (fn.equals("goldmansachs")) return "Goldman Sachs";
        if (fn.equals("morganstanley")) return "Morgan Stanley";
        if (fn.equals("jpmorgan")) return "JPMorgan Chase";
        if (fn.equals("bankofamerica")) return "Bank of America";
        if (fn.equals("wellsfargo")) return "Wells Fargo";
        if (fn.equals("standardchartered")) return "Standard Chartered";
        if (fn.equals("deutschebank")) return "Deutsche Bank";
        if (fn.equals("bnpparibas")) return "BNP Paribas";
        if (fn.equals("schneiderelectric")) return "Schneider Electric";
        if (fn.equals("bostonconsultinggroup") || fn.equals("bcg")) return "Boston Consulting Group (BCG)";
        if (fn.equals("mckinsey")) return "McKinsey & Company";
        if (fn.equals("bain")) return "Bain & Company";
        if (fn.equals("dxctechnology")) return "DXC Technology";
        if (fn.equals("urbancompany")) return "Urban Company";
        if (fn.equals("bigbasket")) return "BigBasket";
        if (fn.equals("phonepe")) return "PhonePe";
        if (fn.equals("freshworks")) return "Freshworks";
        if (fn.equals("servicenow")) return "ServiceNow";
        if (fn.equals("vmware")) return "VMware";
        if (fn.equals("twitter")) return "Twitter / X";
        if (fn.equals("booking")) return "Booking.com";
        return Character.toUpperCase(fn.charAt(0)) + fn.substring(1);
    }

    public static String renderLogoSelectOptions(String selectedPath) {
        StringBuilder sb = new StringBuilder();
        List<String> presets = getPresetLogos();
        for (String p : presets) {
            boolean isSel = p.equalsIgnoreCase(selectedPath);
            sb.append("<option value='").append(p).append("'").append(isSel ? " selected" : "").append(">")
              .append(getDisplayNameForLogo(p)).append(" (").append(p).append(")</option>\n");
        }
        return sb.toString();
    }
}
