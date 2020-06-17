package edu.gatech.chai.fhironfhirbase.analysis;

import java.util.Date;
import java.util.List;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;

public class O2HRAnalysis {
	private static Coding O2Coding = new Coding();
	private static Coding HRCoding = new Coding();
	private Patient patient;
	private Integer O2count;
	private Date O2mostRecentOutOfRange;
	private Float O2averageValue;
	private Integer HRcount;
	private Date HRmostRecentOutOfRange;
	private Float HRaverageValue;
	
	public O2HRAnalysis(Patient patient) {
		this.patient = patient;
		O2count = 0;
		O2mostRecentOutOfRange = new Date();
		O2averageValue = 0.0f;
		HRcount = 0;
		HRmostRecentOutOfRange = new Date();
		HRaverageValue = 0.0f;
	}
	
	public void addNewOccurence(Observation obs) {
		Coding coding = obs.getCode().getCodingFirstRep();
		if(coding.getSystem().equalsIgnoreCase(O2Coding.getSystem()) &&
			coding.getCode().equalsIgnoreCase(O2Coding.getCode())){
			String value = obs.getValueStringType().toString();
			Float percentage = Float.parseFloat(value) / 100;
			//TODO: update the most recent datetime if out of range
			//TODO: update averages
			//TODO: Email if criteria hit
		}
		else if(coding.getSystem().equalsIgnoreCase(HRCoding.getSystem()) &&
				coding.getCode().equalsIgnoreCase(HRCoding.getCode())){
			String value = obs.getValueStringType().toString();
			//TODO: update the most recent datetime if out of range
			//TODO: update averages
			//TODO: Email if criteria hit
		}
	}
}
