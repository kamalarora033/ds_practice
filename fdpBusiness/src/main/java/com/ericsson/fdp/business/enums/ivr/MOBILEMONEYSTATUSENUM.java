package com.ericsson.fdp.business.enums.ivr;

public enum MOBILEMONEYSTATUSENUM {
FAILED("FAILED",-111),
SUCCESSFUL("SUCCESSFUL",0),
PENDING("PENDING",0),

JUNKCODE("JUNK",-9999),

GETTRANSACTION("PENDING",100),

/*NEW CODES*/
	
	ACCOUNT_ALREADY_MEMBER_OF_GROUP("ACCOUNT_ALREADY_MEMBER_OF_GROUP",88),
	ACCOUNT_HOLDER_ADDITIONAL_INFORMATION_NAME_ALREADY_EXIST("ACCOUNT_HOLDER_ADDITIONAL_INFORMATION_NAME_ALREADY_EXIST",89),
	ACCOUNT_HOLDER_ADDITIONAL_INFORMATION_NAME_IN_USE("ACCOUNT_HOLDER_ADDITIONAL_INFORMATION_NAME_IN_USE",90),
	ACCOUNT_HOLDER_ALREADY_MEMBER_OF_GROUP("ACCOUNT_HOLDER_ALREADY_MEMBER_OF_GROUP",91),
	ACCOUNT_HOLDER_DOES_NOT_EXIST("ACCOUNT_HOLDER_DOES_NOT_EXIST",92),
	ACCOUNT_HOLDER_NOT_LOGGED_IN("ACCOUNT_HOLDER_NOT_LOGGED_IN",93),
	ACCOUNT_HOLDER_PROFILE_DOES_NOT_HAVE_REQUESTED_ACCOUNT_TYPE("ACCOUNT_HOLDER_PROFILE_DOES_NOT_HAVE_REQUESTED_ACCOUNT_TYPE",94),
	ACCOUNT_HOLDER_STATUS_IS_NOT_VALID_FOR_ADD_ACCOUNT_OPERATION("ACCOUNT_HOLDER_STATUS_IS_NOT_VALID_FOR_ADD_ACCOUNT_OPERATION",95),
	ACCOUNT_NOT_FOUND("ACCOUNT_NOT_FOUND",96),
	ACCOUNT_ROUTE_ALREADY_EXISTS("ACCOUNT_ROUTE_ALREADY_EXISTS",97),
	ACCOUNT_ROUTE_NOT_FOUND("ACCOUNT_ROUTE_NOT_FOUND",98),
	ACCOUNT_TYPE_NOT_SUPPORTED("ACCOUNT_TYPE_NOT_SUPPORTED",99),
	ACCOUNTHOLDER_ACCOUNT_REFERENCE_ALREADY_EXISTS("ACCOUNTHOLDER_ACCOUNT_REFERENCE_ALREADY_EXISTS",100),
	ACCOUNTHOLDER_ACTIVATION_FAILED("ACCOUNTHOLDER_ACTIVATION_FAILED",101),
	ACCOUNTHOLDER_ANY_ONE_MANDATORY_FIELD_REQUIRED("ACCOUNTHOLDER_ANY_ONE_MANDATORY_FIELD_REQUIRED",102),
	ACCOUNTHOLDER_CAN_NOT_REMOVE_EMAIL("ACCOUNTHOLDER_CAN_NOT_REMOVE_EMAIL",103),
	ACCOUNTHOLDER_CAN_NOT_REMOVE_EXT_ID("ACCOUNTHOLDER_CAN_NOT_REMOVE_EXT_ID",104),
	ACCOUNTHOLDER_CAN_NOT_REMOVE_MSISDN("ACCOUNTHOLDER_CAN_NOT_REMOVE_MSISDN",105),
	ACCOUNTHOLDER_CAN_NOT_REMOVE_USER_NAME("ACCOUNTHOLDER_CAN_NOT_REMOVE_USER_NAME",106),
	ACCOUNTHOLDER_EUI_NOT_FOUND("ACCOUNTHOLDER_EUI_NOT_FOUND",107),
	ACCOUNTHOLDER_MSISDN_NOT_FOUND("ACCOUNTHOLDER_MSISDN_NOT_FOUND",108),
	ACCOUNTHOLDER_NOT_ACTIVE("ACCOUNTHOLDER_NOT_ACTIVE",109),
	ACCOUNTHOLDER_NOT_CHILD("ACCOUNTHOLDER_NOT_CHILD",110),
	ACCOUNTHOLDER_NOT_FOUND("ACCOUNTHOLDER_NOT_FOUND",111),
	ACCOUNTHOLDER_SETTINGS_ALREADY_EXISTS("ACCOUNTHOLDER_SETTINGS_ALREADY_EXISTS",112),
	ACCOUNTHOLDER_SETTINGS_MANDATORY("ACCOUNTHOLDER_SETTINGS_MANDATORY",113),
	ACCOUNTHOLDER_SETTINGS_NOT_FOUND("ACCOUNTHOLDER_SETTINGS_NOT_FOUND",114),
	ACCOUNTHOLDER_WITH_ALIAS_ALREADY_EXISTS("ACCOUNTHOLDER_WITH_ALIAS_ALREADY_EXISTS",115),
	ACCOUNTHOLDER_WITH_EMAIL_ALREADY_EXISTS("ACCOUNTHOLDER_WITH_EMAIL_ALREADY_EXISTS",116),
	ACCOUNTHOLDER_WITH_EMAIL_NOT_FOUND("ACCOUNTHOLDER_WITH_EMAIL_NOT_FOUND",117),
	ACCOUNTHOLDER_WITH_EMPLOYEE_ID_ALREADY_EXISTS("ACCOUNTHOLDER_WITH_EMPLOYEE_ID_ALREADY_EXISTS",118),
	ACCOUNTHOLDER_WITH_EXTERNALID_ALREADY_EXISTS("ACCOUNTHOLDER_WITH_EXTERNALID_ALREADY_EXISTS",119),
	ACCOUNTHOLDER_WITH_FRI_NOT_FOUND("ACCOUNTHOLDER_WITH_FRI_NOT_FOUND",120),
	ACCOUNTHOLDER_WITH_IMSI_ALREADY_EXISTS("ACCOUNTHOLDER_WITH_IMSI_ALREADY_EXISTS",121),
	ACCOUNTHOLDER_WITH_MSISDN_ALREADY_EXISTS("ACCOUNTHOLDER_WITH_MSISDN_ALREADY_EXISTS",122),
	ACCOUNTHOLDER_WITH_MSISDN_NOT_FOUND("ACCOUNTHOLDER_WITH_MSISDN_NOT_FOUND",123),
	ACCOUNTHOLDER_WITH_USERNAME_ALREADY_EXISTS("ACCOUNTHOLDER_WITH_USERNAME_ALREADY_EXISTS",124),
	ADJUSTMENTS_FROM_ACCOUNT_NOT_ALLOWED("ADJUSTMENTS_FROM_ACCOUNT_NOT_ALLOWED",125),
	ADJUSTMENTS_NOT_ALLOWED("ADJUSTMENTS_NOT_ALLOWED",126),
	ADJUSTMENTS_TO_ACCOUNT_NOT_ALLOWED("ADJUSTMENTS_TO_ACCOUNT_NOT_ALLOWED",127),
	ALIAS_NOT_FOUND("ALIAS_NOT_FOUND",128),
	ALREADY_MEMBER_IN_ANOTHER_BANKDOMAIN("ALREADY_MEMBER_IN_ANOTHER_BANKDOMAIN",129),
	AMBIGUOUS_CURRENCY("AMBIGUOUS_CURRENCY",130),
	AMOUNT_INVALID("AMOUNT_INVALID",131),
	AUTHORIZATION_ACCOUNT_NOT_ALLOWED_TO_PERFORM_ACTION("AUTHORIZATION_ACCOUNT_NOT_ALLOWED_TO_PERFORM_ACTION",132),
	AUTHORIZATION_ACCOUNTHOLDER_ALREADY_ACTIVATED("AUTHORIZATION_ACCOUNTHOLDER_ALREADY_ACTIVATED",133),
	AUTHORIZATION_ACCOUNTHOLDER_NOT_ACTIVE("AUTHORIZATION_ACCOUNTHOLDER_NOT_ACTIVE",134),
	AUTHORIZATION_CURRENT_BALANCE_TOO_LOW("AUTHORIZATION_CURRENT_BALANCE_TOO_LOW",135),
	AUTHORIZATION_FAILED("AUTHORIZATION_FAILED",136),
	AUTHORIZATION_INVALID_ACCOUNT_TYPE_COMBINATION("AUTHORIZATION_INVALID_ACCOUNT_TYPE_COMBINATION",137),
	AUTHORIZATION_INVALID_ACCOUNTHOLDER_HIERARCHY("AUTHORIZATION_INVALID_ACCOUNTHOLDER_HIERARCHY",138),
	AUTHORIZATION_INVALID_DEBIT("AUTHORIZATION_INVALID_DEBIT",139),
	AUTHORIZATION_MAX_TRANSFER_ACCOUNT_THROUGHPUT("AUTHORIZATION_MAX_TRANSFER_ACCOUNT_THROUGHPUT",140),
	AUTHORIZATION_MAX_TRANSFER_ACCOUNT_THROUGHPUT_RECEIVER("AUTHORIZATION_MAX_TRANSFER_ACCOUNT_THROUGHPUT_RECEIVER",141),
	AUTHORIZATION_MAX_TRANSFER_ACCOUNT_THROUGHPUT_SENDER("AUTHORIZATION_MAX_TRANSFER_ACCOUNT_THROUGHPUT_SENDER",142),
	AUTHORIZATION_MAX_TRANSFER_AMOUNT("AUTHORIZATION_MAX_TRANSFER_AMOUNT",143),
	AUTHORIZATION_MAX_TRANSFER_AMOUNT_FEE("AUTHORIZATION_MAX_TRANSFER_AMOUNT_FEE",144),
	AUTHORIZATION_MAX_TRANSFER_AMOUNT_FEE_RECEIVER("AUTHORIZATION_MAX_TRANSFER_AMOUNT_FEE_RECEIVER",145),
	AUTHORIZATION_MAX_TRANSFER_AMOUNT_FEE_SENDER("AUTHORIZATION_MAX_TRANSFER_AMOUNT_FEE_SENDER",146),
	AUTHORIZATION_MAX_TRANSFER_AMOUNT_RECEIVER("AUTHORIZATION_MAX_TRANSFER_AMOUNT_RECEIVER",147),
	AUTHORIZATION_MAX_TRANSFER_AMOUNT_SENDER("AUTHORIZATION_MAX_TRANSFER_AMOUNT_SENDER",148),
	AUTHORIZATION_MAX_TRANSFER_TIMES("AUTHORIZATION_MAX_TRANSFER_TIMES",149),
	AUTHORIZATION_MAXIMUM_AMOUNT_ALLOWED_TO_RECEIVE("AUTHORIZATION_MAXIMUM_AMOUNT_ALLOWED_TO_RECEIVE",150),
	AUTHORIZATION_MAXIMUM_AMOUNT_ALLOWED_TO_SEND("AUTHORIZATION_MAXIMUM_AMOUNT_ALLOWED_TO_SEND",151),
	AUTHORIZATION_MAXIMUM_AMOUNT_TO_APPROVE("AUTHORIZATION_MAXIMUM_AMOUNT_TO_APPROVE",152),
	AUTHORIZATION_MINIMUM_AMOUNT_ALLOWED_TO_SEND("AUTHORIZATION_MINIMUM_AMOUNT_ALLOWED_TO_SEND",153),
	AUTHORIZATION_MINIMUM_AMOUNT_ALLOWED_TO_SEND_ACCOUNT("AUTHORIZATION_MINIMUM_AMOUNT_ALLOWED_TO_SEND_ACCOUNT",154),
	AUTHORIZATION_MINIMUM_AMOUNT_ALLOWED_TO_SEND_ACCOUNT_REFERENCE("AUTHORIZATION_MINIMUM_AMOUNT_ALLOWED_TO_SEND_ACCOUNT_REFERENCE",155),
	AUTHORIZATION_NOT_REGISTERED_STATUS("AUTHORIZATION_NOT_REGISTERED_STATUS",156),
	AUTHORIZATION_RECEIVER_ACCOUNT_NO_DEPOSIT("AUTHORIZATION_RECEIVER_ACCOUNT_NO_DEPOSIT",157),
	AUTHORIZATION_RECEIVER_FIRST_DEPOSIT_MINIMUM_AMOUNT_ACCOUNT("AUTHORIZATION_RECEIVER_FIRST_DEPOSIT_MINIMUM_AMOUNT_ACCOUNT",158),
	AUTHORIZATION_RECEIVER_MAX_ALLOWED_BALANCE("AUTHORIZATION_RECEIVER_MAX_ALLOWED_BALANCE",159),
	AUTHORIZATION_RECEIVING_ACCOUNT_NOT_ACTIVE("AUTHORIZATION_RECEIVING_ACCOUNT_NOT_ACTIVE",160),
	AUTHORIZATION_RECEIVING_ACCOUNT_UNAVAILABLE("AUTHORIZATION_RECEIVING_ACCOUNT_UNAVAILABLE",161),
	AUTHORIZATION_SENDER_ACCOUNT_NO_WITHDRAWAL("AUTHORIZATION_SENDER_ACCOUNT_NO_WITHDRAWAL",162),
	AUTHORIZATION_SENDER_ACCOUNT_NOT_ACTIVE("AUTHORIZATION_SENDER_ACCOUNT_NOT_ACTIVE",163),
	AUTHORIZATION_SENDER_MIN_ALLOWED_BALANCE("AUTHORIZATION_SENDER_MIN_ALLOWED_BALANCE",164),
	AUTHORIZATION_SENDING_ACCOUNT_UNAVAILABLE("AUTHORIZATION_SENDING_ACCOUNT_UNAVAILABLE",165),
	BANK_ACCOUNT_NOT_REGISTERED("BANK_ACCOUNT_NOT_REGISTERED",166),
	BANK_ACCOUNT_ROUTE_NOT_FOUND("BANK_ACCOUNT_ROUTE_NOT_FOUND",167),
	BANKDOMAIN_MISSING("BANKDOMAIN_MISSING",168),
	BANKDOMAIN_NOT_FOUND("BANKDOMAIN_NOT_FOUND",169),
	BANKDOMAIN_USED("BANKDOMAIN_USED",170),
	BANKDOMAIN_WITH_NAME_ALREADY_EXISTS("BANKDOMAIN_WITH_NAME_ALREADY_EXISTS",171),
	BLOCK_ACCOUNT_FAILED("BLOCK_ACCOUNT_FAILED",172),
	CAN_NOT_RECEIVE_CASHIN("CAN_NOT_RECEIVE_CASHIN",173),
	CAN_NOT_RECEIVE_CASHOUT("CAN_NOT_RECEIVE_CASHOUT",174),
	CERTIFICATE_ALREADY_UPDATED("CERTIFICATE_ALREADY_UPDATED",175),
	CERTIFICATE_CREDENTIALS_ALREADY_EXIST("CERTIFICATE_CREDENTIALS_ALREADY_EXIST",176),
	CERTIFICATE_CREDENTIALS_NOT_FOUND("CERTIFICATE_CREDENTIALS_NOT_FOUND",177),
	CLOSE_ACCOUNT_FAILED("CLOSE_ACCOUNT_FAILED",178),
	COMPANY_INFORMATION_ALREADY_EXIST("COMPANY_INFORMATION_ALREADY_EXIST",179),
	COMPANY_INFORMATION_DOES_NOT_EXIST("COMPANY_INFORMATION_DOES_NOT_EXIST",180),
	CONTACT_ALREADY_EXISTS("CONTACT_ALREADY_EXISTS",181),
	CONTACT_NOT_FOUND("CONTACT_NOT_FOUND",182),
	COULD_NOT_CREATE_ACCOUNTHOLDER_SETTINGS("COULD_NOT_CREATE_ACCOUNTHOLDER_SETTINGS",183),
	COULD_NOT_PERFORM_ADD_ACCOUNT_OPERATION("COULD_NOT_PERFORM_ADD_ACCOUNT_OPERATION",184),
	COULD_NOT_PERFORM_APPROVAL_OF_OPERATION_PLEASE_RETRY_OPERATION_FROM_BEGINNING("COULD_NOT_PERFORM_APPROVAL_OF_OPERATION_PLEASE_RETRY_OPERATION_FROM_BEGINNING",185),
	COULD_NOT_PERFORM_ATM_PARTIAL_REVERSAL_OPERATION("COULD_NOT_PERFORM_ATM_PARTIAL_REVERSAL_OPERATION",186),
	COULD_NOT_PERFORM_ATM_REDEEM_VOUCHER_OPERATION("COULD_NOT_PERFORM_ATM_REDEEM_VOUCHER_OPERATION",187),
	COULD_NOT_PERFORM_ATM_ROLLBACK_VOUCHER_OPERATION("COULD_NOT_PERFORM_ATM_ROLLBACK_VOUCHER_OPERATION",188),
	COULD_NOT_PERFORM_OPERATION("COULD_NOT_PERFORM_OPERATION",189),
	COULD_NOT_PERFORM_OPERATION_ACCOUNT_NOT_LINKED("COULD_NOT_PERFORM_OPERATION_ACCOUNT_NOT_LINKED",190),
	COULD_NOT_PERFORM_TRANSACTION("COULD_NOT_PERFORM_TRANSACTION",191),
	COULD_NOT_RECEIVE_REMITTANCE("COULD_NOT_RECEIVE_REMITTANCE",192),
	COULD_NOT_UPDATE_ACCOUNTHOLDER_SETTINGS("COULD_NOT_UPDATE_ACCOUNTHOLDER_SETTINGS",193),
	COULD_NOT_UPDATE_PENDING_PAYMENT("COULD_NOT_UPDATE_PENDING_PAYMENT",194),
	COUNTER_CONFIGURATION_NOT_FOUND("COUNTER_CONFIGURATION_NOT_FOUND",195),
	CREDENTIAL_IS_NOT_SUSPENDED("CREDENTIAL_IS_NOT_SUSPENDED",196),
	CREDENTIAL_NOT_ACTIVE("CREDENTIAL_NOT_ACTIVE",197),
	CREDENTIAL_REPEATED_SECRET_DOES_NOT_MATCH("CREDENTIAL_REPEATED_SECRET_DOES_NOT_MATCH",198),
	CREDENTIAL_TYPE_NOT_FOUND("CREDENTIAL_TYPE_NOT_FOUND",199),
	CREDENTIALS_NOT_FOUND("CREDENTIALS_NOT_FOUND",200),
	CREDENTIALS_NOT_FOUND1("CREDENTIALS_NOT_FOUND",201),
	CURRENCIES_DONT_MATCH("CURRENCIES_DONT_MATCH",202),
	CURRENCY_NOT_SUPPORTED("CURRENCY_NOT_SUPPORTED",203),
	CUSTODY_ACCOUNT_ALREADY_EXISTS("CUSTODY_ACCOUNT_ALREADY_EXISTS",204),
	CUSTODY_ACCOUNT_CERTIFICATE_ALREADY_EXIST("CUSTODY_ACCOUNT_CERTIFICATE_ALREADY_EXIST",205),
	CUSTODY_ACCOUNT_CERTIFICATE_COULD_NOT_BE_DELETED("CUSTODY_ACCOUNT_CERTIFICATE_COULD_NOT_BE_DELETED",206),
	CUSTODY_ACCOUNT_CERTIFICATE_COULD_NOT_BE_EMPTY("CUSTODY_ACCOUNT_CERTIFICATE_COULD_NOT_BE_EMPTY",207),
	CUSTODY_ACCOUNT_CERTIFICATE_COULD_NOT_BE_USED_FOR_NON_REPUDIATION("CUSTODY_ACCOUNT_CERTIFICATE_COULD_NOT_BE_USED_FOR_NON_REPUDIATION",208),
	CUSTODY_ACCOUNT_CERTIFICATE_DOES_NOT_EXIST("CUSTODY_ACCOUNT_CERTIFICATE_DOES_NOT_EXIST",209),
	CUSTODY_ACCOUNT_CERTIFICATE_ERROR("CUSTODY_ACCOUNT_CERTIFICATE_ERROR",210),
	CUSTODY_ACCOUNT_CERTIFICATE_NO_SUCH_ALGORITHM("CUSTODY_ACCOUNT_CERTIFICATE_NO_SUCH_ALGORITHM",211),
	CUSTODY_ACCOUNT_CERTIFICATE_WITH_SAME_ID_ALREADY_EXIST("CUSTODY_ACCOUNT_CERTIFICATE_WITH_SAME_ID_ALREADY_EXIST",212),
	CUSTODY_ACCOUNT_NAME_AND_BANKDOMAIN_SAME_AS_BEFORE("CUSTODY_ACCOUNT_NAME_AND_BANKDOMAIN_SAME_AS_BEFORE",213),
	CUSTODY_ACCOUNT_NOT_FOUND("CUSTODY_ACCOUNT_NOT_FOUND",214),
	CUSTODY_ACCOUNT_STILL_CONTAINS_MONEY("CUSTODY_ACCOUNT_STILL_CONTAINS_MONEY",215),
	CUSTODY_ACCOUNT_STILL_HAVE_PENDING_TRANSACTION("CUSTODY_ACCOUNT_STILL_HAVE_PENDING_TRANSACTION",216),
	CUSTODY_ACCOUNT_UPDATION_FAILED_NAME_AND_BANKDOMAIN_MISSING("CUSTODY_ACCOUNT_UPDATION_FAILED_NAME_AND_BANKDOMAIN_MISSING",217),
	DATEFORMAT_INVALID("DATEFORMAT_INVALID",218),
	DIFFERENT_BANKDOMAINS("DIFFERENT_BANKDOMAINS",219),
	DOCUMENT_DOES_NOT_EXIST("DOCUMENT_DOES_NOT_EXIST",220),
	DOCUMENT_URL_CONFIGURATION_ERROR("DOCUMENT_URL_CONFIGURATION_ERROR",221),
	DOCUMENT_URL_INVALID("DOCUMENT_URL_INVALID",222),
	DOCUMENT_URL_NOT_ALLOWED("DOCUMENT_URL_NOT_ALLOWED",223),
	END_DATE_IN_FUTURE("END_DATE_IN_FUTURE",224),
	EXPIRED_OR_INVALID_TRANSACTION_ID("EXPIRED_OR_INVALID_TRANSACTION_ID",225),
	EXTERNAL_ERROR_MESSAGE("EXTERNAL_ERROR_MESSAGE",226),
	EXTERNAL_SERVICE_PROVIDER_NOT_FOUND("EXTERNAL_SERVICE_PROVIDER_NOT_FOUND",227),
	FAILED_TO_CREATE_NETTING_REPORT("FAILED_TO_CREATE_NETTING_REPORT",228),
	FIELD_TOO_LONG("FIELD_TOO_LONG",229),
	FINANCIAL_CONFIGURATION_DOES_NOT_EXIST("FINANCIAL_CONFIGURATION_DOES_NOT_EXIST",230),
	FINANCIAL_CONTROLLER_TRANSACTION_NOT_FOUND("FINANCIAL_CONTROLLER_TRANSACTION_NOT_FOUND",231),
	FIRST_NAME_NOT_ALLOWED_IN_PASSWORD("FIRST_NAME_NOT_ALLOWED_IN_PASSWORD",232),
	FLOAT_TRANSFER_EXEMPTION_ALREADY_EXISTS("FLOAT_TRANSFER_EXEMPTION_ALREADY_EXISTS",233),
	FRI_CAN_ONLY_HAVE_ONE_OWNER("FRI_CAN_ONLY_HAVE_ONE_OWNER",234),
	FRI_INVALID("FRI_INVALID",235),
	FRI_REFERENCE_NOT_FOUND("FRI_REFERENCE_NOT_FOUND",236),
	FROM_DATE_AFTER_TO_DATE("FROM_DATE_AFTER_TO_DATE",237),
	GROUP_ALREADY_USED_BY_ANOTHER_BANKDOMAIN("GROUP_ALREADY_USED_BY_ANOTHER_BANKDOMAIN",238),
	GROUP_MEMBER_COULD_NOT_BE_REMOVED("GROUP_MEMBER_COULD_NOT_BE_REMOVED",239),
	GROUP_MEMBER_COULD_NOT_BE_REMOVED_FROM_BANKDOMAIN("GROUP_MEMBER_COULD_NOT_BE_REMOVED_FROM_BANKDOMAIN",240),
	GROUP_MEMBER_NOT_FOUND("GROUP_MEMBER_NOT_FOUND",241),
	GROUP_USED_BY_BANKDOMAIN("GROUP_USED_BY_BANKDOMAIN",242),
	HIGH_CONTENTION_ACCOUNT_NOT_SUPPORTED_AS_MIRRORED_ACCOUNT("HIGH_CONTENTION_ACCOUNT_NOT_SUPPORTED_AS_MIRRORED_ACCOUNT",243),
	HOME_CHARGING_REGION_NOT_FOUND("HOME_CHARGING_REGION_NOT_FOUND",244),
	HOME_CHARGING_REGION_USED("HOME_CHARGING_REGION_USED",245),
	HOME_CHARGING_REGION_WITH_NAME_ALREADY_EXISTS("HOME_CHARGING_REGION_WITH_NAME_ALREADY_EXISTS",246),
	ID_LENGTH_OUT_OF_BOUNDS("ID_LENGTH_OUT_OF_BOUNDS",247),
	IDENTIFICATION_ID_AND_TYPE_ALREADY_EXIST("IDENTIFICATION_ID_AND_TYPE_ALREADY_EXIST",248),
	IDENTITY_INVALID("IDENTITY_INVALID",249),
	IDTYPE_NOT_SUPPORT("IDTYPE_NOT_SUPPORT",250),
	ILLEGAL_CREDENTIAL_STATUS_CHANGE("ILLEGAL_CREDENTIAL_STATUS_CHANGE",251),
	IMSI_INVALID("IMSI_INVALID",252),
	INACTIVE_ACCOUNT("INACTIVE_ACCOUNT",253),
	INCORRECT_PASSWORD("INCORRECT_PASSWORD",254),
	INCORRECT_PIN("INCORRECT_PIN",255),
	INCORRECT_SEQURITY_ANSWER("INCORRECT_SEQURITY_ANSWER",256),
	INCORRECT_TRANSACTION_STATUS("INCORRECT_TRANSACTION_STATUS",257),
	INTERNAL_ERROR("INTERNAL_ERROR",258),
	INTERNAL_LOAN_ACCOUNT_NOT_FOUND("INTERNAL_LOAN_ACCOUNT_NOT_FOUND",259),
	INTERNAL_LOAN_AMOUNT_MIN_LARGER_THAN_AMOUNT_MAX("INTERNAL_LOAN_AMOUNT_MIN_LARGER_THAN_AMOUNT_MAX",260),
	INTERNAL_LOAN_APPLICATION_IS_NOT_VALID("INTERNAL_LOAN_APPLICATION_IS_NOT_VALID",261),
	INTERNAL_LOAN_APPLICATION_NOT_FOUND("INTERNAL_LOAN_APPLICATION_NOT_FOUND",262),
	INTERNAL_LOAN_MAXIMUM_NUMBER_OF_APPLICATIONS("INTERNAL_LOAN_MAXIMUM_NUMBER_OF_APPLICATIONS",263),
	INTERNAL_LOAN_PRODUCT_ALREADY_EXIST("INTERNAL_LOAN_PRODUCT_ALREADY_EXIST",264),
	INTERNAL_LOAN_PRODUCT_IS_NOT_ACTIVE("INTERNAL_LOAN_PRODUCT_IS_NOT_ACTIVE",265),
	INTERNAL_LOAN_PRODUCT_NOT_ALLOWED("INTERNAL_LOAN_PRODUCT_NOT_ALLOWED",266),
	INTERNAL_LOAN_PRODUCT_NOT_FOUND("INTERNAL_LOAN_PRODUCT_NOT_FOUND",267),
	INTERNAL_LOAN_PROVIDER_ALREADY_EXIST("INTERNAL_LOAN_PROVIDER_ALREADY_EXIST",268),
	INTERNAL_LOAN_PROVIDER_NOT_FOUND("INTERNAL_LOAN_PROVIDER_NOT_FOUND",269),
	INVALID_ACCOUNT_FRI("INVALID_ACCOUNT_FRI",270),
	INVALID_ACCOUNT_HOLDER_ADDITIONAL_INFORMATION_NAME("INVALID_ACCOUNT_HOLDER_ADDITIONAL_INFORMATION_NAME",271),
	INVALID_ACCOUNTHOLDER_SETTINGS("INVALID_ACCOUNTHOLDER_SETTINGS",272),
	INVALID_APPROVAL_TRANSACTION_STATUS("INVALID_APPROVAL_TRANSACTION_STATUS",273),
	INVALID_ATM_OPERATION("INVALID_ATM_OPERATION",274),
	INVALID_CERTIFICATE("INVALID_CERTIFICATE",275),
	INVALID_CHOSEN_DELIVERY_METHOD("INVALID_CHOSEN_DELIVERY_METHOD",276),
	INVALID_CURRENCY("INVALID_CURRENCY",277),
	INVALID_FIELD_VALUE("INVALID_FIELD_VALUE",278),
	INVALID_PROFILE("INVALID_PROFILE",279),
	INVALID_PROFILE_COUNTER("INVALID_PROFILE_COUNTER",280),
	INVALID_PROFILE_NAME("INVALID_PROFILE_NAME",281),
	INVALID_PROFILE_THRESHOLD("INVALID_PROFILE_THRESHOLD",282),
	INVALID_RECEIVER("INVALID_RECEIVER",283),
	INVALID_REMITTANCE_STATUS("INVALID_REMITTANCE_STATUS",284),
	INVALID_SCHEDULED_TRANSACTION_DATE("INVALID_SCHEDULED_TRANSACTION_DATE",285),
	INVALID_SCHEDULED_TRANSACTION_STATUS("INVALID_SCHEDULED_TRANSACTION_STATUS",286),
	INVALID_SETTLEMENT_DEPOSIT_ID("INVALID_SETTLEMENT_DEPOSIT_ID",287),
	INVALID_SETTLEMENTID("INVALID_SETTLEMENTID",288),
	INVALID_SIGNATURE("INVALID_SIGNATURE",289),
	INVALID_STATUS("INVALID_STATUS",290),
	INVALID_TRANSACTION_TYPE("INVALID_TRANSACTION_TYPE",291),
	INVALID_USER_APPROVAL_TYPE("INVALID_USER_APPROVAL_TYPE",292),
	LAST_BANKDOMAIN_NOT_ALLOWED_TO_REMOVE("LAST_BANKDOMAIN_NOT_ALLOWED_TO_REMOVE",293),
	LIMITED_RETRIES_FOR_SERVICE_PROVIDER_NOT_ALLOWED("LIMITED_RETRIES_FOR_SERVICE_PROVIDER_NOT_ALLOWED",294),
	LOAN_APPLICATION_NOT_FOUND("LOAN_APPLICATION_NOT_FOUND",295),
	LOAN_PROVIDER_NOT_FOUND("LOAN_PROVIDER_NOT_FOUND",296),
	MANDATORY_FIELD_MISSING("MANDATORY_FIELD_MISSING",297),
	MAX_ACCOUNTS_LIMIT_REACHED("MAX_ACCOUNTS_LIMIT_REACHED",298),
	MAX_NUMBER_OF_CONTACTS_EXCEEDED("MAX_NUMBER_OF_CONTACTS_EXCEEDED",299),
	MAX_NUMBER_OF_DOCUMENT_EXCEEDED("MAX_NUMBER_OF_DOCUMENT_EXCEEDED",300),
	MISSING_ACTOR_MSISDN_FOR_EXTERNAL_RATING("MISSING_ACTOR_MSISDN_FOR_EXTERNAL_RATING",301),
	MISSING_EXCHANGE_RATE("MISSING_EXCHANGE_RATE",302),
	MORE_THAN_ONE_MUTUALLY_EXCLUSIVE_OPTIONAL_PARAMETERS_SET("MORE_THAN_ONE_MUTUALLY_EXCLUSIVE_OPTIONAL_PARAMETERS_SET",303),
	MSISDN_ALREADY_USED_BY_POINTOFSALE("MSISDN_ALREADY_USED_BY_POINTOFSALE",304),
	NO_ACCESS("NO_ACCESS",305),
	NONASCII_CHARACTERS_IN_PASSWORD("NONASCII_CHARACTERS_IN_PASSWORD",306),
	NONDIGIT_CHARACTERS_IN_PINCODE("NONDIGIT_CHARACTERS_IN_PINCODE",307),
	NOT_ALLOWED_TO_CHANGE_BANKDOMAIN("NOT_ALLOWED_TO_CHANGE_BANKDOMAIN",308),
	NOT_AUTHORIZED("NOT_AUTHORIZED",309),
	NOT_ENOUGH_FUNDS("NOT_ENOUGH_FUNDS",310),
	ONE_CUSTODY_ACCOUNT_MUST_EXIST("ONE_CUSTODY_ACCOUNT_MUST_EXIST",311),
	ONLY_ONE_REFUND_PER_DEBIT_IS_ALLOWED("ONLY_ONE_REFUND_PER_DEBIT_IS_ALLOWED",312),
	OPERATION_NOT_SUPPORTED_ON_EXTERNAL_ACCOUNT("OPERATION_NOT_SUPPORTED_ON_EXTERNAL_ACCOUNT",313),
	OPERATION_NOT_SUPPORTED_ON_MIGRATED_ACCOUNT("OPERATION_NOT_SUPPORTED_ON_MIGRATED_ACCOUNT",314),
	OTHER_FINANCIAL_ERROR("OTHER_FINANCIAL_ERROR",315),
	OTP_FOR_BANK_ACCOUNT_VERIFICATION_MISSING("OTP_FOR_BANK_ACCOUNT_VERIFICATION_MISSING",316),
	OTP_FOR_EMAIL_VERIFICATION_MISSING("OTP_FOR_EMAIL_VERIFICATION_MISSING",317),
	OTP_FOR_MSISDN_VERIFICATION_MISSING("OTP_FOR_MSISDN_VERIFICATION_MISSING",318),
	OTP_INVALID("OTP_INVALID",319),
	OTP_NOT_FOUND("OTP_NOT_FOUND",320),
	PASSWORD_ALREADY_USED_BEFORE("PASSWORD_ALREADY_USED_BEFORE",321),
	PASSWORD_TOO_SHORT("PASSWORD_TOO_SHORT",322),
	PENDING_COMMISSION_NOT_FOUND("PENDING_COMMISSION_NOT_FOUND",323),
	PICKUP_REFERENCE_ID_NOT_FOUND("PICKUP_REFERENCE_ID_NOT_FOUND",324),
	PINCODE_ALREADY_USED_BEFORE("PINCODE_ALREADY_USED_BEFORE",325),
	PINCODE_DENIED_BY_MATCHING_BIRTH_DATE("PINCODE_DENIED_BY_MATCHING_BIRTH_DATE",326),
	PINCODE_DENIED_BY_MATCHING_IDENTIFICATION_NUMBER("PINCODE_DENIED_BY_MATCHING_IDENTIFICATION_NUMBER",327),
	PINCODE_DENIED_BY_MATCHING_MSISDN("PINCODE_DENIED_BY_MATCHING_MSISDN",328),
	PINCODE_DENIED_BY_MATCHING_REGULAR_EXPRESSION("PINCODE_DENIED_BY_MATCHING_REGULAR_EXPRESSION",329),
	PINCODE_HAS_EXPIRED("PINCODE_HAS_EXPIRED",330),
	PINCODE_TOO_LONG("PINCODE_TOO_LONG",331),
	PINCODE_TOO_SHORT("PINCODE_TOO_SHORT",332),
	POINTOFSALE_NOT_ACQUIRED("POINTOFSALE_NOT_ACQUIRED",333),
	PREAPPROVAL_NOT_FOUND("PREAPPROVAL_NOT_FOUND",334),
	PREAPPROVALS_NOT_FOUND("PREAPPROVALS_NOT_FOUND",335),
	PROFILE_ALREADY_EXISTS("PROFILE_ALREADY_EXISTS",336),
	PROFILE_JSON_INVALID_FILE("PROFILE_JSON_INVALID_FILE",337),
	PROFILE_MISMATCH("PROFILE_MISMATCH",338),
	PROFILE_NOT_FOUND("PROFILE_NOT_FOUND",339),
	PROFILE_TYPE_MISMATCH("PROFILE_TYPE_MISMATCH",340),
	PROFILE_VERSION_MANDATORY("PROFILE_VERSION_MANDATORY",341),
	PROFILE_VERSION_MISMATCH("PROFILE_VERSION_MISMATCH",342),
	PROFILE_WITH_NAME_DOES_NOT_EXIST("PROFILE_WITH_NAME_DOES_NOT_EXIST",343),
	PROVIDER_CATEGORY_LINKED_WITH_ACCOUNT_HOLDER("PROVIDER_CATEGORY_LINKED_WITH_ACCOUNT_HOLDER",344),
	PROVIDER_CATEGORY_NOT_FOUND("PROVIDER_CATEGORY_NOT_FOUND",345),
	QUEUED_FOR_APPROVAL("QUEUED_FOR_APPROVAL",346),
	QUOTE_ALREADY_USED("QUOTE_ALREADY_USED",347),
	QUOTE_EXPIRED("QUOTE_EXPIRED",348),
	QUOTE_NOT_FOUND("QUOTE_NOT_FOUND",349),
	QUOTE_NOT_SUPPORTED("QUOTE_NOT_SUPPORTED",350),
	QUOTE_PROCESSING("QUOTE_PROCESSING",351),
	REACTIVATE_ACCOUNT_FAILED("REACTIVATE_ACCOUNT_FAILED",352),
	RECONCILIATION_PROCESS_ERROR("RECONCILIATION_PROCESS_ERROR",353),
	RECURRING_SCHEDULED_TRANSACTION_EXCEEDS_MAX_DAYS("RECURRING_SCHEDULED_TRANSACTION_EXCEEDS_MAX_DAYS",354),
	REFERENCE_ID_ALREADY_IN_USE("REFERENCE_ID_ALREADY_IN_USE",355),
	REFERENCE_ID_NOT_FOUND("REFERENCE_ID_NOT_FOUND",356),
	REFUND_AMOUNT_CANNOT_EXCEED_THE_DEBIT_AMOUNT("REFUND_AMOUNT_CANNOT_EXCEED_THE_DEBIT_AMOUNT",357),
	RELATIVE_DOES_NOT_EXIST("RELATIVE_DOES_NOT_EXIST",358),
	REMITTANCE_ID_DOES_NOT_EXIST("REMITTANCE_ID_DOES_NOT_EXIST",359),
	REMITTANCE_NOT_FOUND("REMITTANCE_NOT_FOUND",360),
	REMITTANCE_NOT_POSSIBLE("REMITTANCE_NOT_POSSIBLE",361),
	REMOVE_PARENT_NOT_SUPPORTED_FOR_NONLEAF_CHILD("REMOVE_PARENT_NOT_SUPPORTED_FOR_NONLEAF_CHILD",362),
	REQUIRED_FIELD_MISSING("REQUIRED_FIELD_MISSING",363),
	REQUIRED_PASSWORD_CRITERIA_NOT_MATCH("REQUIRED_PASSWORD_CRITERIA_NOT_MATCH",364),
	RESOURCE_NOT_ACTIVE("RESOURCE_NOT_ACTIVE",365),
	RESOURCE_NOT_AVAILABLE("RESOURCE_NOT_AVAILABLE",366),
	RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND",367),
	RESOURCE_TEMPORARY_LOCKED("RESOURCE_TEMPORARY_LOCKED",368),
	REVERSAL_ALREADY_PERFORMED_ON_THIS_TRANSACTION("REVERSAL_ALREADY_PERFORMED_ON_THIS_TRANSACTION",369),
	REVERSAL_NOT_ALLOWED_ON_THIS_TRANSACTION_TYPE("REVERSAL_NOT_ALLOWED_ON_THIS_TRANSACTION_TYPE",370),
	REVERSAL_SUB_TRANSACTION_INVALID("REVERSAL_SUB_TRANSACTION_INVALID",371),
	REVERSED_USER_ID_NOT_ALLOWED_IN_PASSWORD("REVERSED_USER_ID_NOT_ALLOWED_IN_PASSWORD",372),
	ROLLBACK_NOT_POSSIBLE("ROLLBACK_NOT_POSSIBLE",373),
	SCHEDULED_TRANSACTION_EXCEEDS_MAX_DAYS("SCHEDULED_TRANSACTION_EXCEEDS_MAX_DAYS",374),
	SCHEDULED_TRANSACTION_NOT_FOUND("SCHEDULED_TRANSACTION_NOT_FOUND",375),
	SELF_SERVICE_NOTIFICATION_DUPLICATE("SELF_SERVICE_NOTIFICATION_DUPLICATE",376),
	SELF_SERVICE_NOTIFICATION_LIMIT_EXCEEDS_HARD_LIMIT("SELF_SERVICE_NOTIFICATION_LIMIT_EXCEEDS_HARD_LIMIT",377),
	SELF_SERVICE_NOTIFICATION_NOT_FOUND("SELF_SERVICE_NOTIFICATION_NOT_FOUND",378),
	SELF_SERVICE_NOTIFICATION_TOO_MANY("SELF_SERVICE_NOTIFICATION_TOO_MANY",379),
	SERVICE_TEMPORARILY_UNAVAILABLE("SERVICE_TEMPORARILY_UNAVAILABLE",380),
	SESSION_INVALID("SESSION_INVALID",381),
	SETTLEMENT_AMOUNT_DO_NOT_MATCH("SETTLEMENT_AMOUNT_DO_NOT_MATCH",382),
	SETTLEMENT_ANOTHER_SETTLEMENT_BEING_PROCESSED("SETTLEMENT_ANOTHER_SETTLEMENT_BEING_PROCESSED",383),
	SETTLEMENT_CLOSING_BALANCE_MISMATCH("SETTLEMENT_CLOSING_BALANCE_MISMATCH",384),
	SETTLEMENT_CONFIGURATION_CERTIFICATE_DOES_NOT_EXIST_FOR_CUSTODY_ACCOUNT("SETTLEMENT_CONFIGURATION_CERTIFICATE_DOES_NOT_EXIST_FOR_CUSTODY_ACCOUNT",385),
	SETTLEMENT_CONFIGURATION_CUSTODY_ACCOUNT_ALREADY_EXISTS_WITH_SETTLEMENT_CONFIGURATION("SETTLEMENT_CONFIGURATION_CUSTODY_ACCOUNT_ALREADY_EXISTS_WITH_SETTLEMENT_CONFIGURATION",386),
	SETTLEMENT_CONFIGURATION_DOES_NOT_EXIST_FOR_CUSTODY_ACCOUNT("SETTLEMENT_CONFIGURATION_DOES_NOT_EXIST_FOR_CUSTODY_ACCOUNT",387),
	SETTLEMENT_CONFIGURATION_FOR_IDENTIFIER_ALREADY_EXISTS("SETTLEMENT_CONFIGURATION_FOR_IDENTIFIER_ALREADY_EXISTS",388),
	SETTLEMENT_CONFIGURATION_INSTRUCTION_FORMATTER_CLASS_NOT_FOUND("SETTLEMENT_CONFIGURATION_INSTRUCTION_FORMATTER_CLASS_NOT_FOUND",389),
	SETTLEMENT_CONFIGURATION_NOT_FOUND("SETTLEMENT_CONFIGURATION_NOT_FOUND",390),
	SETTLEMENT_CONFIGURATION_SETTLEMENT_PARSER_CLASS_NOT_FOUND("SETTLEMENT_CONFIGURATION_SETTLEMENT_PARSER_CLASS_NOT_FOUND",391),
	SETTLEMENT_CONFIGURATION_UNABLE_TO_REMOVE_CUSTODY_ACCOUNT_ID("SETTLEMENT_CONFIGURATION_UNABLE_TO_REMOVE_CUSTODY_ACCOUNT_ID",392),
	SETTLEMENT_CURRENCY_DO_NOT_MATCH("SETTLEMENT_CURRENCY_DO_NOT_MATCH",393),
	SETTLEMENT_CUSTODY_ACCOUNT_DO_NOT_MATCH("SETTLEMENT_CUSTODY_ACCOUNT_DO_NOT_MATCH",394),
	SETTLEMENT_DEPOSIT_ALREADY_REVERSED("SETTLEMENT_DEPOSIT_ALREADY_REVERSED",395),
	SETTLEMENT_DEPOSIT_IN_INVALID_STATE("SETTLEMENT_DEPOSIT_IN_INVALID_STATE",396),
	SETTLEMENT_DEPOSIT_NOT_FOUND("SETTLEMENT_DEPOSIT_NOT_FOUND",397),
	SETTLEMENT_DEPOSIT_TRANSACTION_DETAILS_MISSING("SETTLEMENT_DEPOSIT_TRANSACTION_DETAILS_MISSING",398),
	SETTLEMENT_DUPLICATE_RECORD_FOUND("SETTLEMENT_DUPLICATE_RECORD_FOUND",399),
	SETTLEMENT_FILE_DIGEST_VERIFICATION_ERROR("SETTLEMENT_FILE_DIGEST_VERIFICATION_ERROR",400),
	SETTLEMENT_FILE_FORMAT_ERROR("SETTLEMENT_FILE_FORMAT_ERROR",401),
	SETTLEMENT_FILE_SIGNATURE_VERIFICATION_ERROR("SETTLEMENT_FILE_SIGNATURE_VERIFICATION_ERROR",402),
	SETTLEMENT_IDENTIFIER_DO_NOT_MATCH("SETTLEMENT_IDENTIFIER_DO_NOT_MATCH",403),
	SETTLEMENT_IDENTIFIER_NOT_FOUND("SETTLEMENT_IDENTIFIER_NOT_FOUND",404),
	SETTLEMENT_INVALID_AMOUNT("SETTLEMENT_INVALID_AMOUNT",405),
	SETTLEMENT_INVALID_RECEIVER("SETTLEMENT_INVALID_RECEIVER",406),
	SETTLEMENT_NOT_READY("SETTLEMENT_NOT_READY",407),
	SETTLEMENT_ONLINE_SETTLEMENT_NOT_ENABLED_FOR_CUSTODY_ACCOUNT("SETTLEMENT_ONLINE_SETTLEMENT_NOT_ENABLED_FOR_CUSTODY_ACCOUNT",408),
	SETTLEMENT_OPENING_BALANCE_MISMATCH("SETTLEMENT_OPENING_BALANCE_MISMATCH",409),
	SETTLEMENT_PAYMENT_INSTRUCTION_IN_WRONG_STATE("SETTLEMENT_PAYMENT_INSTRUCTION_IN_WRONG_STATE",410),
	SETTLEMENT_PAYMENT_INSTRUCTION_NOT_FOUND("SETTLEMENT_PAYMENT_INSTRUCTION_NOT_FOUND",411),
	SETTLEMENT_PROCESS_ERROR("SETTLEMENT_PROCESS_ERROR",412),
	SETTLEMENT_RECEIVING_ACCOUNT_DO_NOT_MATCH("SETTLEMENT_RECEIVING_ACCOUNT_DO_NOT_MATCH",413),
	SETTLEMENT_TRANSFER_IN_WRONG_STATE("SETTLEMENT_TRANSFER_IN_WRONG_STATE",414),
	SOURCE_AND_TARGET_ARE_THE_SAME("SOURCE_AND_TARGET_ARE_THE_SAME",415),
	SOURCE_NOT_FOUND("SOURCE_NOT_FOUND",416),
	SURNAME_NOT_ALLOWED_IN_PASSWORD("SURNAME_NOT_ALLOWED_IN_PASSWORD",417),
	SUSPEND_ACCOUNT_FAILED("SUSPEND_ACCOUNT_FAILED",418),
	TAG_WITH_NAME_ALREADY_EXISTS("TAG_WITH_NAME_ALREADY_EXISTS",419),
	TAG_WITH_NAME_DO_NOT_EXISTS("TAG_WITH_NAME_DO_NOT_EXISTS",420),
	TARGET_AUTHORIZATION_ERROR("TARGET_AUTHORIZATION_ERROR",421),
	TARGET_NOT_FOUND("TARGET_NOT_FOUND",422),
	TAX_REPORT_NOT_FOUND("TAX_REPORT_NOT_FOUND",423),
	THRESHOLD_CONFIGURATION_NOT_FOUND("THRESHOLD_CONFIGURATION_NOT_FOUND",424),
	TOO_FEW_LOWERCASE_ALPHABETIC_CHARACTERS_IN_PASSWORD("TOO_FEW_LOWERCASE_ALPHABETIC_CHARACTERS_IN_PASSWORD",425),
	TOO_FEW_NUMERICAL_DIGITS_IN_PASSWORD("TOO_FEW_NUMERICAL_DIGITS_IN_PASSWORD",426),
	TOO_FEW_SPECIAL_CHARACTERS_IN_PASSWORD("TOO_FEW_SPECIAL_CHARACTERS_IN_PASSWORD",427),
	TOO_FEW_UPPERCASE_ALPHABETIC_CHARACTERS_IN_PASSWORD("TOO_FEW_UPPERCASE_ALPHABETIC_CHARACTERS_IN_PASSWORD",428),
	TOO_FEW_UPPERCASE_OR_LOWERCASE_ALPHABETIC_CHARACTERS_IN_PASSWORD("TOO_FEW_UPPERCASE_OR_LOWERCASE_ALPHABETIC_CHARACTERS_IN_PASSWORD",429),
	TOO_MANY_CONSECUTIVE_DIGITS_IN_PINCODE("TOO_MANY_CONSECUTIVE_DIGITS_IN_PINCODE",430),
	TOO_MANY_IDENTICAL_CONSECUTIVE_CHARACTERS_IN_PASSWORD("TOO_MANY_IDENTICAL_CONSECUTIVE_CHARACTERS_IN_PASSWORD",431),
	TOO_MANY_REPEATED_DIGITS_IN_PINCODE("TOO_MANY_REPEATED_DIGITS_IN_PINCODE",432),
	TRANSACTION_ID_OR_ORIGINAL_TRANSACTION_ID_MISSING("TRANSACTION_ID_OR_ORIGINAL_TRANSACTION_ID_MISSING",433),
	TRANSACTION_NOT_COMPLETED("TRANSACTION_NOT_COMPLETED",434),
	TRANSACTION_NOT_FOUND("TRANSACTION_NOT_FOUND",435),
	TRANSACTION_TIMED_OUT("TRANSACTION_TIMED_OUT",436),
	TRANSFER_TYPE_AND_ACCOUNT_DO_NOT_MATCH("TRANSFER_TYPE_AND_ACCOUNT_DO_NOT_MATCH",437),
	TRANSFERTOBANK_NOT_SUPPORTED("TRANSFERTOBANK_NOT_SUPPORTED",438),
	UNABLE_TO_CREATE_ACCOUNTHOLDER("UNABLE_TO_CREATE_ACCOUNTHOLDER",439),
	UNABLE_TO_CREATE_INTEREST_TIER("UNABLE_TO_CREATE_INTEREST_TIER",440),
	UNABLE_TO_MODIFY_INTEREST_TIER("UNABLE_TO_MODIFY_INTEREST_TIER",441),
	UNABLE_TO_REMOVE_INTEREST_TIER("UNABLE_TO_REMOVE_INTEREST_TIER",442),
	UNBLOCK_ACCOUNT_FAILED("UNBLOCK_ACCOUNT_FAILED",443),
	UNRESOLVED_REMITTANCE_ID_DOES_NOT_EXIST("UNRESOLVED_REMITTANCE_ID_DOES_NOT_EXIST",444),
	UNSUSPEND_CREDENTIAL_COUNTER_THRESHOLD_REACHED("UNSUSPEND_CREDENTIAL_COUNTER_THRESHOLD_REACHED",445),
	USER_ID_NOT_ALLOWED_IN_PASSWORD("USER_ID_NOT_ALLOWED_IN_PASSWORD",446),
	VALIDATION_ERROR("VALIDATION_ERROR",447),
	VOUCHER_DATE_OF_BIRTH_SET("VOUCHER_DATE_OF_BIRTH_SET",448),
	VOUCHER_EXPIRED("VOUCHER_EXPIRED",449),
	VOUCHER_EXTERNALID_SET("VOUCHER_EXTERNALID_SET",450),
	VOUCHER_FIRSTNAME_SET("VOUCHER_FIRSTNAME_SET",451),
	VOUCHER_NOT_ACTIVE("VOUCHER_NOT_ACTIVE",452),
	VOUCHER_NOT_FOUND("VOUCHER_NOT_FOUND",453),
	VOUCHER_SECRET_CONTAINS_EXCLUDED_LETTERS("VOUCHER_SECRET_CONTAINS_EXCLUDED_LETTERS",454),
	VOUCHER_SECRET_CONTAINS_ILLEGAL_CHARACTERS("VOUCHER_SECRET_CONTAINS_ILLEGAL_CHARACTERS",455),
	VOUCHER_SECRET_CONTAINS_NONASCII_CHARACTERS("VOUCHER_SECRET_CONTAINS_NONASCII_CHARACTERS",456),
	VOUCHER_SECRET_DIGITS_NOT_ALLOWED("VOUCHER_SECRET_DIGITS_NOT_ALLOWED",457),
	VOUCHER_SECRET_INVALID("VOUCHER_SECRET_INVALID",458),
	VOUCHER_SECRET_IS_EMPTY("VOUCHER_SECRET_IS_EMPTY",459),
	VOUCHER_SECRET_LETTERS_NOT_ALLOWED("VOUCHER_SECRET_LETTERS_NOT_ALLOWED",460),
	VOUCHER_SECRET_SYSTEM_CONFIGURATION_ERROR("VOUCHER_SECRET_SYSTEM_CONFIGURATION_ERROR",461),
	VOUCHER_SECRET_TOO_LONG("VOUCHER_SECRET_TOO_LONG",462),
	VOUCHER_SECRET_TOO_MANY_FAILED_REDEEM_ATTEMPTS("VOUCHER_SECRET_TOO_MANY_FAILED_REDEEM_ATTEMPTS",463),
	VOUCHER_SECRET_TOO_SHORT("VOUCHER_SECRET_TOO_SHORT",464),
	VOUCHER_SURNAME_SET("VOUCHER_SURNAME_SET",465),
	WRONG_PROFILE("WRONG_PROFILE",466),
	COUPONS_AWARDER_NOT_EXIST("COUPONS_AWARDER_NOT_EXIST",467),
	COUPONS_COUPON_NOT_FOUND("COUPONS_COUPON_NOT_FOUND",468),
	COUPONS_COUPONS_PER_USER_GREATER_THAN_TOTAL_COUPONS("COUPONS_COUPONS_PER_USER_GREATER_THAN_TOTAL_COUPONS",469),
	COUPONS_CURRENCIES_DONT_MATCH("COUPONS_CURRENCIES_DONT_MATCH",470),
	COUPONS_END_DATE_PRECEDES_START_DATE("COUPONS_END_DATE_PRECEDES_START_DATE",471),
	COUPONS_EXPIRED_COUPON("COUPONS_EXPIRED_COUPON",472),
	COUPONS_EXPIRED_OFFER("COUPONS_EXPIRED_OFFER",473),
	COUPONS_INVALID_COUPONS_PER_USER_COUNT("COUPONS_INVALID_COUPONS_PER_USER_COUNT",474),
	COUPONS_INVALID_FUNDING_ACCOUNT("COUPONS_INVALID_FUNDING_ACCOUNT",475),
	COUPONS_INVALID_OFFER_STATUS("COUPONS_INVALID_OFFER_STATUS",476),
	COUPONS_INVALID_REDEEM_FUNDS("COUPONS_INVALID_REDEEM_FUNDS",477),
	COUPONS_INVALID_REWARD("COUPONS_INVALID_REWARD",478),
	COUPONS_INVALID_TOTAL_COUPONS_COUNT("COUPONS_INVALID_TOTAL_COUPONS_COUNT",479),
	COUPONS_MIN_AMOUNT_LARGER_THAN_MAX("COUPONS_MIN_AMOUNT_LARGER_THAN_MAX",480),
	COUPONS_MISMATCHING_CURRENCIES("COUPONS_MISMATCHING_CURRENCIES",481),
	COUPONS_OFFER_ALLOWANCE_REACHED("COUPONS_OFFER_ALLOWANCE_REACHED",482),
	COUPONS_OFFER_NOT_AVAILABLE("COUPONS_OFFER_NOT_AVAILABLE",483),
	COUPONS_OFFER_NOT_FOUND("COUPONS_OFFER_NOT_FOUND",484),
	COUPONS_PURCHASE_AMOUNT_TOO_HIGH("COUPONS_PURCHASE_AMOUNT_TOO_HIGH",485),
	COUPONS_PURCHASE_AMOUNT_TOO_LOW("COUPONS_PURCHASE_AMOUNT_TOO_LOW",486),
	COUPONS_REWARD_NOT_SET("COUPONS_REWARD_NOT_SET",487),
	COUPONS_REWARDTYPE_NOT_SUPPORTED("COUPONS_REWARDTYPE_NOT_SUPPORTED",488),
	COUPONS_USER_ALLOWANCE_REACHED("COUPONS_USER_ALLOWANCE_REACHED",489),
	COUPONS_USER_NOT_ELIGIBLE_FOR_OFFER("COUPONS_USER_NOT_ELIGIBLE_FOR_OFFER",490),
	COUPONS_USER_PROFILE_NOT_EXIST("COUPONS_USER_PROFILE_NOT_EXIST",491),
	ABOVE_MAXIMUM_CARD_BALANCE("ABOVE_MAXIMUM_CARD_BALANCE",492),
	ACCOUNT_HOLDER_UNAUTHORIZED_TO_HAVE_LINKED_CARD("ACCOUNT_HOLDER_UNAUTHORIZED_TO_HAVE_LINKED_CARD",493),
	ACCOUNT_HOLDER_UNAUTHORIZED_TO_UNBLOCK_CARD("ACCOUNT_HOLDER_UNAUTHORIZED_TO_UNBLOCK_CARD",494),
	ACCOUNT_TYPE_NOT_SUPPORTED1("ACCOUNT_TYPE_NOT_SUPPORTED",495),
	ACCOUNTHOLDER_NOT_ALLOWED_TO_WITHDRAW_FROM_FRI("ACCOUNTHOLDER_NOT_ALLOWED_TO_WITHDRAW_FROM_FRI",496),
	ACCOUNTHOLDER_NOT_CONNECTED_TO_FRI("ACCOUNTHOLDER_NOT_CONNECTED_TO_FRI",497),
	BELOW_MINIMUM_CARD_BALANCE("BELOW_MINIMUM_CARD_BALANCE",498),
	CARD_ALREADY_ACTIVE("CARD_ALREADY_ACTIVE",499),
	CARD_ALREADY_LINKED("CARD_ALREADY_LINKED",500),
	CARD_BALANCE_EXCEEDED("CARD_BALANCE_EXCEEDED",501),
	CARD_BLOCKED("CARD_BLOCKED",502),
	CARD_EXPIRED("CARD_EXPIRED",503),
	CARD_FINALIZED("CARD_FINALIZED",504),
	CARD_LOST("CARD_LOST",505),
	CARD_NOT_ACTIVE("CARD_NOT_ACTIVE",506),
	CARD_NOT_AVAILABLE("CARD_NOT_AVAILABLE",507),
	CARD_NOT_BLOCKED("CARD_NOT_BLOCKED",508),
	CARD_NOT_FOUND("CARD_NOT_FOUND",509),
	CARD_PROCESSOR_NOT_AVAILABLE("CARD_PROCESSOR_NOT_AVAILABLE",510),
	CARD_PROVIDER_NOT_AVAILABLE("CARD_PROVIDER_NOT_AVAILABLE",511),
	CARD_RESERVATION_NOT_FOUND("CARD_RESERVATION_NOT_FOUND",512),
	CARD_RESERVATION_ROLLEDBACK("CARD_RESERVATION_ROLLEDBACK",513),
	CARD_RESERVATION_UNKNOWN_STATUS("CARD_RESERVATION_UNKNOWN_STATUS",514),
	CARD_STOLEN("CARD_STOLEN",515),
	CARD_TERMINATED("CARD_TERMINATED",516),
	CARD_WAS_NOT_BLOCKED("CARD_WAS_NOT_BLOCKED",517),
	CARD_WAS_NOT_TERMINATED("CARD_WAS_NOT_TERMINATED",518),
	COMMUNICATION_ERROR("COMMUNICATION_ERROR",519),
	CREATE_CARD_FAILED("CREATE_CARD_FAILED",520),
	INCOMPATIBLE_CARD("INCOMPATIBLE_CARD",521),
	INCOMPATIBLE_TEMPLATE("INCOMPATIBLE_TEMPLATE",522),
	INVALID_AMOUNT("INVALID_AMOUNT",523),
	INVALID_CAMPAIGN_ID("INVALID_CAMPAIGN_ID",524),
	INVALID_CARD_NUMBER("INVALID_CARD_NUMBER",525),
	INVALID_CURRENCY1("INVALID_CURRENCY",526),
	INVALID_PERIOD("INVALID_PERIOD",527),
	INVALID_TEMPLATE("INVALID_TEMPLATE",528),
	MAX_PHYSICAL_CARDS_EXCEEDED("MAX_PHYSICAL_CARDS_EXCEEDED",529),
	MAX_PHYSICAL_CARDS_EXCEEDED_PERIOD("MAX_PHYSICAL_CARDS_EXCEEDED_PERIOD",530),
	MAX_VIRTUAL_CARDS_EXCEEDED("MAX_VIRTUAL_CARDS_EXCEEDED",531),
	MAX_VIRTUAL_CARDS_EXCEEDED_PERIOD("MAX_VIRTUAL_CARDS_EXCEEDED_PERIOD",532),
	MAXIMUM_VALIDITY_TIME_EXCEEDED("MAXIMUM_VALIDITY_TIME_EXCEEDED",533),
	NOT_A_CARD_FRI("NOT_A_CARD_FRI",534),
	NOT_SUPPORTED("NOT_SUPPORTED",535),
	OWNER_NOT_ACTIVE("OWNER_NOT_ACTIVE",536),
	REVERSAL_NOT_POSSIBLE("REVERSAL_NOT_POSSIBLE",537),
	REVERSAL_NOT_POSSIBLE_ALREADY_REVERSED("REVERSAL_NOT_POSSIBLE_ALREADY_REVERSED",538),
	REVERSAL_NOT_POSSIBLE_CARD_MISMATCH("REVERSAL_NOT_POSSIBLE_CARD_MISMATCH",539),
	REVERSAL_NOT_POSSIBLE_INVALID_AMOUNT("REVERSAL_NOT_POSSIBLE_INVALID_AMOUNT",540),
	REVERSAL_NOT_POSSIBLE_PARTIAL_REVERSAL_NOT_ALLOWED("REVERSAL_NOT_POSSIBLE_PARTIAL_REVERSAL_NOT_ALLOWED",541),
	REVERSAL_NOT_POSSIBLE_TRANSACTION_NOT_FOUND("REVERSAL_NOT_POSSIBLE_TRANSACTION_NOT_FOUND",542),
	TERMINATE_NOT_POSSIBLE_FINALIZED_CARD("TERMINATE_NOT_POSSIBLE_FINALIZED_CARD",543),
	UNKNOWN_ERROR("UNKNOWN_ERROR",544),
	UNSUPPORTED_BLOCKING_REASON("UNSUPPORTED_BLOCKING_REASON",545),
	UNSUPPORTED_CARD_TYPE("UNSUPPORTED_CARD_TYPE",546),
	UNSUPPORTED_TERMINATION_REASON("UNSUPPORTED_TERMINATION_REASON",547),
	VIRTUAL_CARD_CREATION_BALANCE_ABOVE_MAX_LIMIT("VIRTUAL_CARD_CREATION_BALANCE_ABOVE_MAX_LIMIT",548),
	VIRTUAL_CARD_CREATION_BALANCE_BELOW_MIN_LIMIT("VIRTUAL_CARD_CREATION_BALANCE_BELOW_MIN_LIMIT",549),
	AUTHENTICATION_FAILED("AUTHENTICATION_FAILED",550),
	CARD_EXPIRED1("CARD_EXPIRED",551),
	COMMUNICATION_ERROR1("COMMUNICATION_ERROR",552),
	GENERAL_ERROR("GENERAL_ERROR",553),
	INVALID_CARD_NUMBER1("INVALID_CARD_NUMBER",554),
	INVALID_CARD_PIN("INVALID_CARD_PIN",555),
	INVALID_CARD_REFERENCE_NUMBER("INVALID_CARD_REFERENCE_NUMBER",556),
	INVALID_CARD_STATUS_CHANGE("INVALID_CARD_STATUS_CHANGE",557),
	INVALID_CARD_TRACKING_NUMBER("INVALID_CARD_TRACKING_NUMBER",558),
	INVALID_CUSTOMER_DATA("INVALID_CUSTOMER_DATA",559),
	INVALID_PROVIDER_NAME("INVALID_PROVIDER_NAME",560),
	

/*Errors in error code*/;

private String key;
private Integer value;

MOBILEMONEYSTATUSENUM(String strkey,Integer strvalue)
{
key=strkey;
value=strvalue;

}

public String getKey()
{
return key;
}

public Integer getValue()
{
return value;
}

}
