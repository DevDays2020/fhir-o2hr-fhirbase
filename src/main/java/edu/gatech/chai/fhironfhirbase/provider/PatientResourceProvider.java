/*******************************************************************************
 * Copyright (c) 2019 Georgia Tech Research Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package edu.gatech.chai.fhironfhirbase.provider;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.springframework.stereotype.Service;
import org.hl7.fhir.r4.model.Organization;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import edu.gatech.chai.fhironfhirbase.model.USCorePatient;
import edu.gatech.chai.fhironfhirbase.utilities.ThrowFHIRExceptions;

/**
 * This is a resource provider which stores Patient resources in memory using a
 * HashMap. This is obviously not a production-ready solution for many reasons,
 * but it is useful to help illustrate how to build a fully-functional server.
 */
@Service
public class PatientResourceProvider extends BaseResourceProvider {

	public PatientResourceProvider() {
		super();
	}

	@PostConstruct
	private void postConstruct() {
		setTableName(PatientResourceProvider.getType().toLowerCase());
		setMyResourceType(PatientResourceProvider.getType());
		getTotalSize("SELECT count(*) FROM " + getTableName() + ";");
	}

	public static String getType() {
		return "Patient";
	}

	/**
	 * The getResourceType method comes from IResourceProvider, and must be
	 * overridden to indicate what type of resource this provider supplies.
	 */
	@Override
	public Class<USCorePatient> getResourceType() {
		return USCorePatient.class;
	}

	/**
	 * The "@Create" annotation indicates that this method implements "create=type",
	 * which adds a new instance of a resource to the server.
	 */
	@Create()
	public MethodOutcome createPatient(@ResourceParam USCorePatient thePatient) {
		validateResource(thePatient);
		MethodOutcome retVal = new MethodOutcome();

		try {
			IBaseResource createdPatient = getFhirbaseMapping().create(thePatient, getResourceType());
			retVal.setId(createdPatient.getIdElement());
			retVal.setResource(createdPatient);
			retVal.setCreated(true);
		} catch (SQLException e) {
			retVal.setCreated(false);
			e.printStackTrace();
		}

		return retVal;
	}

	/**
	 * This is the "read" operation. The "@Read" annotation indicates that this
	 * method supports the read and/or vread operation.
	 * <p>
	 * Read operations take a single parameter annotated with the {@link IdParam}
	 * paramater, and should return a single resource instance.
	 * </p>
	 * 
	 * @param theId The read operation takes one parameter, which must be of type
	 *              IdDt and must be annotated with the "@Read.IdParam" annotation.
	 * @return Returns a resource matching this identifier, or null if none exists.
	 */
	@Read()
	public IBaseResource readPatient(@IdParam IdType theId) {
		IBaseResource retVal = null;

		try {
			retVal = getFhirbaseMapping().read(theId, getResourceType(), getTableName());
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return retVal;
	}

	/**
	 * The "@Update" annotation indicates that this method supports replacing an
	 * existing resource (by ID) with a new instance of that resource.
	 * 
	 * @param theId      This is the ID of the patient to update
	 * @param thePatient This is the actual resource to save
	 * @return This method returns a "MethodOutcome"
	 */
	@Update()
	public MethodOutcome updatePatient(@IdParam IdType theId, @ResourceParam USCorePatient thePatient) {
		validateResource(thePatient);
		MethodOutcome retVal = new MethodOutcome();

		try {
			IBaseResource updatedPatient = getFhirbaseMapping().update(thePatient, getResourceType());
			retVal.setId(updatedPatient.getIdElement());
			retVal.setResource(updatedPatient);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return retVal;
	}

	@Delete()
	public void deletePatient(@IdParam IdType theId) {
		try {
			getFhirbaseMapping().delete(theId, getResourceType(), getTableName());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The "@Search" annotation indicates that this method supports the search
	 * operation. You may have many different method annotated with this annotation,
	 * to support many different search criteria. This example searches by family
	 * name.
	 * 
	 * @param theFamilyName This operation takes one parameter which is the search
	 *                      criteria. It is annotated with the "@Required"
	 *                      annotation. This annotation takes one argument, a string
	 *                      containing the name of the search criteria. The datatype
	 *                      here is StringParam, but there are other possible
	 *                      parameter types depending on the specific search
	 *                      criteria.
	 * @return This method returns a list of Patients in bundle. This list may
	 *         contain multiple matching resources, or it may also be empty.
	 */
	@Search(allowUnknownParams = true)
	public IBundleProvider findPatientsByParams(RequestDetails theRequestDetails,
			@OptionalParam(name = USCorePatient.SP_RES_ID) TokenParam thePatientId,
			@OptionalParam(name = USCorePatient.SP_IDENTIFIER) TokenParam thePatientIdentifier,
			@OptionalParam(name = USCorePatient.SP_ACTIVE) TokenParam theActive,
			@OptionalParam(name = USCorePatient.SP_FAMILY) StringParam theFamilyName,
			@OptionalParam(name = USCorePatient.SP_GIVEN) StringParam theGivenName,
			@OptionalParam(name = USCorePatient.SP_NAME) StringParam theName,
			@OptionalParam(name = USCorePatient.SP_GENDER) StringParam theGender,
			@OptionalParam(name = USCorePatient.SP_BIRTHDATE) DateParam theBirthDate,
			@OptionalParam(name = USCorePatient.SP_ADDRESS) StringParam theAddress,
			@OptionalParam(name = USCorePatient.SP_ADDRESS_CITY) StringParam theAddressCity,
			@OptionalParam(name = USCorePatient.SP_ADDRESS_STATE) StringParam theAddressState,
			@OptionalParam(name = USCorePatient.SP_ADDRESS_POSTALCODE) StringParam theAddressZip,
			@OptionalParam(name = USCorePatient.SP_EMAIL) TokenParam theEmail,
			@OptionalParam(name = USCorePatient.SP_PHONE) TokenParam thePhone,
			@OptionalParam(name = USCorePatient.SP_TELECOM) TokenParam theTelecom,
			@OptionalParam(name = USCorePatient.SP_ORGANIZATION, chainWhitelist = { "",
					Organization.SP_NAME }) ReferenceParam theOrganization,
			@Sort SortSpec theSort,

			@IncludeParam(allow = { "Patient:general-practitioner", "Patient:organization",
					"Patient:link" }) final Set<Include> theIncludes,

			@IncludeParam(allow = { "Encounter:subject",
					"Observation:subject" }, reverse = true) final Set<Include> theReverseIncludes) {

		List<String> whereParameters = new ArrayList<String>();

		String fromStatement = "patient p";

		if (thePatientId != null) {
			whereParameters.add("p.id = '" + thePatientId.getValue() + "'");
		}

		if (thePatientIdentifier != null) {
			String system = thePatientIdentifier.getSystem();
			String value = thePatientIdentifier.getValue();

			if (system != null && !system.isEmpty() && value != null && !value.isEmpty()) {
				whereParameters.add("p.resource->'identifier' @> '[{\"value\": \"" + value + "\",\"system\": \""
						+ system + "\"}]'::jsonb");
			} else if (system != null && !system.isEmpty() && (value == null || value.isEmpty())) {
				whereParameters.add("p.resource->'identifier' @> '[{\"system\": \"" + system + "\"}]'::jsonb");
			} else if ((system == null || system.isEmpty()) && value != null && !value.isEmpty()) {
				whereParameters.add("p.resource->'identifier' @> '[{\"value\": \"" + value + "\"}]'::jsonb");
			}
		}
		if (theActive != null) {
			whereParameters.add("p.resource->>'active'=" + theActive.getValue());
		}
		if (theEmail != null) {
			whereParameters.add(
					"p.resource->'telecom' @> '[{\"system\":\"email\"}]'::jsonb AND p.resource->'telecom' @> '[{\"value\":\""
							+ theEmail.getValue() + "\"}]'::jsonb");
		}
		if (thePhone != null) {
			whereParameters.add(
					"p.resource->'telecom' @> '[{\"system\":\"phone\"}]'::jsonb AND p.resource->'telecom' @> '[{\"value\":\""
							+ thePhone.getValue() + "\"}]'::jsonb");
		}
		if (theTelecom != null) {
			whereParameters.add("p.resource->'telecom' @> '[{\"system\":\"" + theTelecom.getSystem()
					+ "\"}]'::jsonb AND p.resource->'telecom' @> '[{\"value\":\"" + theTelecom.getValue()
					+ "\"}]'::jsonb");
		}
		if (theFamilyName != null) {
			if (!fromStatement.contains("names")) {
				fromStatement += ", jsonb_array_elements(p.resource->'name') names";
			}

			if (theFamilyName.isExact()) {
				whereParameters.add("names->>'family' = '" + theFamilyName.getValue() + "'");
			} else {
				whereParameters.add("names->>'family' like '%" + theFamilyName.getValue() + "%'");
			}
		}
		if (theName != null) {
			whereParameters.add("p.resource->>'name' like '%" + theName.getValue() + "%'");
		}
		if (theGivenName != null) {
			if (!fromStatement.contains("names")) {
				fromStatement += ", jsonb_array_elements(p.resource->'name') names";
			}

			whereParameters.add("names->>'given' like '%" + theGivenName.getValue() + "%'");
		}
		if (theGender != null) {
			whereParameters.add("pract.resource->>'gender' = '" + theGender.getValue() + "'");
		}
		if (theBirthDate != null) {
			String where = constructDateWhereParameter(theBirthDate, "p", "birthDate");
			if (where != null && !where.isEmpty()) {
				whereParameters.add(where);
			}
		}
		if (theAddress != null) {
			whereParameters.add("p.resource->>'address' like '%" + theAddress.getValue() + "%'");
		}
		if (theAddressCity != null) {
			if (!fromStatement.contains("addresses")) {
				fromStatement += ", jsonb_array_elements(p.resource->'address') addresses";
			}

			if (theAddressCity.isExact()) {
				whereParameters.add("addresses->>'city' = '" + theAddressCity.getValue() + "'");
			} else {
				whereParameters.add("addresses->>'city' like '%" + theAddressCity.getValue() + "%'");
			}
		}
		if (theAddressState != null) {
			if (!fromStatement.contains("addresses")) {
				fromStatement += ", jsonb_array_elements(p.resource->'address') addresses";
			}

			if (theAddressState.isExact()) {
				whereParameters.add("addresses->>'state' = '" + theAddressState.getValue() + "'");
			} else {
				whereParameters.add("addresses->>'state' like '%" + theAddressState.getValue() + "%'");
			}
		}
		if (theAddressZip != null) {
			if (!fromStatement.contains("addresses")) {
				fromStatement += ", jsonb_array_elements(p.resource->'address') addresses";
			}

			if (theAddressZip.isExact()) {
				whereParameters.add("addresses->>'postalCode' = '" + theAddressZip.getValue() + "'");
			} else {
				whereParameters.add("addresses->>'postalCode' like '%" + theAddressZip.getValue() + "%'");
			}
		}

		// Chain Search.
		// Chain search is a searching by reference with specific field name (including
		// reference ID).
		// As SP names are not unique across the FHIR resources, we need to tag the name
		// of the resource in front to indicate our OMOP* can handle these parameters.
		// TODO: do this organization chain search later.
//		if (theOrganization != null) {
//			String orgChain = theOrganization.getChain();
//			if (orgChain != null) {
//				if (Organization.SP_NAME.equals(orgChain)) {
//					String theOrgName = theOrganization.getValue();
//					paramList.addAll(
//							getMyMapper().mapParameter("Organization:" + Organization.SP_NAME, theOrgName, false));
//				} else if ("".equals(orgChain)) {
//					paramList.addAll(getMyMapper().mapParameter("Organization:" + Organization.SP_RES_ID,
//							theOrganization.getValue(), false));
//				}
//			} else {
//				paramList.addAll(getMyMapper().mapParameter("Organization:" + Organization.SP_RES_ID,
//						theOrganization.getIdPart(), false));
//			}
//		}

		// Complete Query.
		String whereStatement = constructWhereStatement(whereParameters, theSort);

		String queryCount = "SELECT count(*) FROM " + fromStatement + whereStatement;
		String query = "SELECT * FROM " + fromStatement + whereStatement;
		MyBundleProvider myBundleProvider = new MyBundleProvider(query, theIncludes, theReverseIncludes);
		myBundleProvider.setTotalSize(getTotalSize(queryCount));
		myBundleProvider.setPreferredPageSize(preferredPageSize);

		return myBundleProvider;

	}

	/**
	 * $everything operation for a single patient.
	 */
	@Operation(name = "$everything", idempotent = true, bundleType = BundleTypeEnum.SEARCHSET)
	public IBundleProvider patientEverythingOperation(RequestDetails theRequestDetails, @IdParam IdType thePatientId,
			@OperationParam(name = "start") DateType theStart, @OperationParam(name = "end") DateType theEnd) {

		if (thePatientId == null) {
			ThrowFHIRExceptions.unprocessableEntityException("Patient Id must be present");
		}

		Date startDate = null;
		if (theStart != null)
			startDate = theStart.getValue();

		Date endDate = null;
		if (theEnd != null)
			endDate = theEnd.getValue();

		List<IBaseResource> resources = new ArrayList<IBaseResource>();
		try {
			resources.add(getFhirbaseMapping().read(thePatientId, getResourceType(), "patient"));
			
			// get compositions
			String sql = "select * from composition c where c.resource->'subject'->>'reference' like '%Patient/"
					+ thePatientId.getIdPart() + "'";
			resources.addAll(getFhirbaseMapping().search(sql, Composition.class));
			
			// Get conditions
			sql = "select * from condition c where c.resource->'subject'->>'reference' like '%Patient/"
					+ thePatientId.getIdPart() + "'";
			resources.addAll(getFhirbaseMapping().search(sql, Condition.class));

			// Get devices
			sql = "select * from device d where d.resource->'patient'->>'reference' like '%Patient/"
					+ thePatientId.getIdPart() + "'";
			resources.addAll(getFhirbaseMapping().search(sql, Device.class));

		} catch (Exception e) {
			e.printStackTrace();
			ThrowFHIRExceptions.internalErrorException("Failed to read the patient Id");
		}

		getEverthingfor(resources, thePatientId, startDate, endDate);

		final List<IBaseResource> retv = resources;
		final Integer totalsize = retv.size();
		final Integer pageSize = totalsize; // Some clients do not support pagination. preferredPageSize;

		return new IBundleProvider() {
			@Override
			public IPrimitiveType<Date> getPublished() {
				return null;
			}

			@Override
			public List<IBaseResource> getResources(int theFromIndex, int theToIndex) {
				return retv.subList(theFromIndex, theToIndex);
			}

			@Override
			public String getUuid() {
				return null;
			}

			@Override
			public Integer preferredPageSize() {
				return pageSize;
			}

			@Override
			public Integer size() {
				return totalsize;
			}
		};
	}

	private void getEverthingfor(List<IBaseResource> resources, IdType thePatientId, Date startDate, Date endDate) {

	}

	/**
	 * This method just provides simple business validation for resources we are
	 * storing.
	 * 
	 * @param thePatient The patient to validate
	 */
	private void validateResource(USCorePatient thePatient) {
		/*
		 * Our server will have a rule that patients must have a family name or we will
		 * reject them
		 */
		if (thePatient.getNameFirstRep().getFamily().isEmpty()) {
			OperationOutcome outcome = new OperationOutcome();
			CodeableConcept detailCode = new CodeableConcept();
			detailCode.setText("No family name provided, Patient resources must have at least one family name.");
			outcome.addIssue().setSeverity(IssueSeverity.FATAL).setDetails(detailCode);
			throw new UnprocessableEntityException(FhirContext.forR4(), outcome);
		}
	}

	class MyBundleProvider extends FhirbaseBundleProvider implements IBundleProvider {
		Set<Include> theIncludes;
		Set<Include> theReverseIncludes;

		public MyBundleProvider(String query, Set<Include> theIncludes, Set<Include> theReverseIncludes) {
			super(query);
			setPreferredPageSize(preferredPageSize);
			this.theIncludes = theIncludes;
			this.theReverseIncludes = theReverseIncludes;
		}

		@Override
		public List<IBaseResource> getResources(int fromIndex, int toIndex) {
			List<IBaseResource> retv = new ArrayList<IBaseResource>();

			if (toIndex - fromIndex > 0) {
				query += " LIMIT " + (toIndex - fromIndex) + " OFFSET " + fromIndex;
			}

			try {
				retv.addAll(getFhirbaseMapping().search(query, getResourceType()));
			} catch (SQLException e) {
				e.printStackTrace();
			}

			// TODO: _Include and _ReverseInclude - do this later.
			// _Include
			List<String> includes = new ArrayList<String>();
			if (theIncludes.contains(new Include("Patient:general-practitioner"))) {
				includes.add("Patient:general-practitioner");
			}

			if (theIncludes.contains(new Include("Patient:organization"))) {
				includes.add("Patient:organization");
			}

			if (theIncludes.contains(new Include("Patient:link"))) {
				includes.add("Patient:link");
			}

			if (theReverseIncludes.contains(new Include("*"))) {
				// This is to include all the reverse includes...
				includes.add("Encounter:subject");
				includes.add("Observation:subject");
				includes.add("Device:patient");
				includes.add("Condition:subject");
				includes.add("Procedure:subject");
				includes.add("MedicationRequest:subject");
				includes.add("MedicationAdministration:subject");
				includes.add("MedicationDispense:subject");
				includes.add("MedicationStatement:subject");
			} else {
				if (theReverseIncludes.contains(new Include("Encounter:subject"))) {
					includes.add("Encounter:subject");
				}
				if (theReverseIncludes.contains(new Include("Observation:subject"))) {
					includes.add("Observation:subject");
				}
				if (theReverseIncludes.contains(new Include("Device:patient"))) {
					includes.add("Device:patient");
				}
				if (theReverseIncludes.contains(new Include("Condition:subject"))) {
					includes.add("Condition:subject");
				}
				if (theReverseIncludes.contains(new Include("Procedure:subject"))) {
					includes.add("Procedure:subject");
				}
				if (theReverseIncludes.contains(new Include("MedicationRequest:subject"))) {
					includes.add("MedicationRequest:subject");
				}
				if (theReverseIncludes.contains(new Include("MedicationAdministration:subject"))) {
					includes.add("MedicationAdministration:subject");
				}
				if (theReverseIncludes.contains(new Include("MedicationDispense:subject"))) {
					includes.add("MedicationDispense:subject");
				}
				if (theReverseIncludes.contains(new Include("MedicationStatement:subject"))) {
					includes.add("MedicationStatement:subject");
				}
			}

			return retv;
		}

	}

}