package com.ericsson.fdp.AirConfig.parser;

public enum ParsingConstant {

	// for getting Name
	TARIFFWITHSETFIELDMODIFIER(
			"/EreExport/ServiceProviderList/ServiceProvider/RatingPlan/RatingPeriod/TariffStructure/Node[contains(text(),'Voucherless')]/Node/Node/Tariff[contains(text(),'SetFieldModifier')]/../Condition[contains(text(),'RefillProfileID')]/Property[@name='RightValue']/@value"), TARIFFWITHSETSEGMENTAIONIDMODIFIER(
			"/EreExport/ServiceProviderList/ServiceProvider/RatingPlan/RatingPeriod/TariffStructure/Node[contains(text(),'Voucherless')]/Node/Node/Node/Tariff[contains(text(),'SetSegmentationIdModifier')]/../../Condition[contains(text(),'RefillProfileID')]/Property[@name='RightValue']/@value"),

	// for getting IDs
	TARIFFIDWITHSETFIELDMODIFIER(
			"/EreExport/ServiceProviderList/ServiceProvider/RatingPlan/RatingPeriod/TariffStructure/Node[contains(text(),'Voucherless')]/Node/Node/Condition[contains(text(),'RefillProfileID')]/../Tariff/Property[@name='ValueValue']/@value"), TARIFFIDWITHSETSEGMENTAIONIDMODIFIER(
			"/EreExport/ServiceProviderList/ServiceProvider/RatingPlan/RatingPeriod/TariffStructure/Node[contains(text(),'Voucherless')]/Node/Node/Node/Tariff[contains(text(),'SetSegmentationIdModifier')]/Property[@name='SegmentationID']/@value"),

	// for getting the service id
	SERVICE_IDS(
			"/EreExport/ServiceProviderList/ServiceProvider/RatingPlan/RatingPeriod/TariffStructure/Node[contains(text(),'RefillSegmentation')]/Tariff[contains(text(),'RefillSegmentationModifier')]/Config/Matrix/SegmentationMatrix/List/s/@id"), SERVICE_VALUES(
			"/EreExport/ServiceProviderList/ServiceProvider/RatingPlan/RatingPeriod/TariffStructure/Node[contains(text(),'RefillSegmentation')]/Tariff[contains(text(),'RefillSegmentationModifier')]/Config/Matrix/SegmentationMatrix/List/s/text()"),

	REFILLIDFOROFFER(
			"/EreExport/ServiceProviderList/ServiceProvider/RatingPlan/RatingPeriod/TariffStructure/Node/Condition[contains(text(),'RefillSelectionID')]/Property[@name='RightValue'][@value='{AP_1}']/../../Tariff[contains(text(),'InstallOfferTypeModifier')]/Property[@name='Index']/@value"), REFILLVALUEFOROFFER(
			"/EreExport/ServiceProviderList/ServiceProvider/RatingPlan/RatingPeriod/TariffStructure/Node/Condition[contains(text(),'RefillSelectionID')]/Property[@name='RightValue'][@value='{AP_1}']/../../Tariff[contains(text(),'InstallOfferTypeModifier')]/Property[@name='Type']/@value"),

	ACCOUNTDIVISIONMODIFIERID(
			"/EreExport/ServiceProviderList/ServiceProvider/RatingPlan/RatingPeriod/TariffStructure/Node/Condition[contains(text(),'RefillSelectionID')]/Property[@name='RightValue'][@value='{AP_1}']/../../Tariff[contains(text(),'AccountDivisionModifier')]/Property[@name='AdditionalConfig']/@value"),

	DA_IDS(
			"/EreExport/ServiceProviderList/ServiceProvider/RatingPlan/RatingPeriod/TariffStructure/Node[contains(text(),'Account Division')]/Node/Condition[contains(text(),'AccountDivisionID')]/Property[@name='RightValue'][@value='{AP_1}']/../../Tariff[contains(text(),'AddFixedAmountModifier')]/Property[@name='OutputFieldIndex']/@value"),

	// for getting files
	REFILLFILE("/EreExport/ServiceList/Service[@DefinitionFile='Refill']"), ACCOUNTREFILLFILE(
			"/EreExport/ServiceList/Service[@DefinitionFile='Account refill']"), ACCOUNTDIVISIONFILE(
			"/EreExport/ServiceList/Service[@DefinitionFile='Account division']"),

	// file path
	FILEPATH("C:/Users/ehlnopu/Desktop/file");
	String _value;

	ParsingConstant(String value) {
		_value = value;
	}

	public String get_value() {
		return _value;
	};

}
