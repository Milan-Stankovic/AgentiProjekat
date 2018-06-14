package encoderDecoder;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import dto.OdlazniWsDTO;

public class OdlazniWsDTOEncoder implements Encoder.Text<OdlazniWsDTO> {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(EndpointConfig arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String encode(OdlazniWsDTO o) throws EncodeException {
		
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json="";
		try {
			json = ow.writeValueAsString(o);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json;
	}

}
