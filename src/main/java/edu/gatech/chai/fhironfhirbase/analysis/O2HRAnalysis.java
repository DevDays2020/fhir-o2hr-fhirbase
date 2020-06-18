package edu.gatech.chai.fhironfhirbase.analysis;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Quantity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

public class O2HRAnalysis {
    public static Coding O2Coding = new Coding("http://loinc.org", "2708-6", "OxygenSat");
    public static Coding HRCoding = new Coding("http://loinc.org", "8867-4", "HeartRate");
    private Patient patient;
    private Integer O2count;
    private Date O2mostRecentOutOfRange;
    private Float O2averageValue;
    private Integer HRcount;
    private Date HRmostRecentOutOfRange;
    private Float HRaverageValue;
    private Quantity O2Quantity;
    private Quantity HRQuantity;


    // Email stuff
    private static final String EMAIL_SENDER = "10devdays2020@gmail.com";
    private static final String EMAIL_SENDER_PASSWORD = "10devdays2020#@!";


    public O2HRAnalysis(/*Patient patient*/) {
        this.patient = patient;
        O2count = 0;
        O2mostRecentOutOfRange = new Date();
        O2averageValue = 0.0f;
        HRcount = 0;
        HRmostRecentOutOfRange = new Date();
        HRaverageValue = 0.0f;
    }

    public void addNewOccurence(Observation obs) throws Exception {
        Coding coding = obs.getCode().getCodingFirstRep();
        if (coding.getSystem().equalsIgnoreCase(O2Coding.getSystem()) &&
                coding.getCode().equalsIgnoreCase(O2Coding.getCode())) {
			O2count += 1;

            String value = obs.getValueQuantity().getValue().toString();
            Float percentage = Float.parseFloat(value) / 100;
            //TODO: update the most recent datetime if out of range
            if (percentage < .92) {
                O2mostRecentOutOfRange = new Date();
                EmailSender javaEmail = new EmailSender();
                javaEmail.setMailServerProperties();
                javaEmail.draftEmailMessage("");
                javaEmail.sendEmail(String.format("O2 Value %1$d out of range on %2$s", Integer.parseInt(value), O2mostRecentOutOfRange.toString()), EMAIL_SENDER, EMAIL_SENDER_PASSWORD);
            }

            //TODO: update averages
			O2averageValue = (O2averageValue + Integer.parseInt(value))/O2count;

        } else if (coding.getSystem().equalsIgnoreCase(HRCoding.getSystem()) &&
                coding.getCode().equalsIgnoreCase(HRCoding.getCode())) {
            HRcount += 1;

            String value = obs.getValueQuantity().getValue().toString();
            Float percentage = Float.parseFloat(value) / 100;
            //TODO: update the most recent datetime if out of range
            if (Integer.parseInt(value) < 84) {
                HRmostRecentOutOfRange = new Date();
                EmailSender javaEmail = new EmailSender();
                javaEmail.setMailServerProperties();
                javaEmail.draftEmailMessage("");
                javaEmail.sendEmail(String.format("HR Value %1$d out of range on %2$s", Integer.parseInt(value), HRmostRecentOutOfRange.toString()), EMAIL_SENDER, EMAIL_SENDER_PASSWORD);
            }
            //TODO: update averages
			HRaverageValue = (HRaverageValue + Integer.parseInt(value))/HRcount;
        }
    }


}
