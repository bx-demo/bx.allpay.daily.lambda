package bx.allpay.daily.lambda;

class Request_return {
	String soapxml;
	int record_count;
	protected void set_soapxml(String xml_string) {
		this.soapxml=xml_string;
	}
	protected void set_count(int count) {
		this.record_count=count;
	}

}