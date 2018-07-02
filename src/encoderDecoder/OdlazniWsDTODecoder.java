package encoderDecoder;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import com.google.gson.Gson;

import dto.DolazniWsDTO;
import dto.OdlazniWsDTO;

public class OdlazniWsDTODecoder implements Decoder.Text<OdlazniWsDTO> {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(EndpointConfig arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public OdlazniWsDTO decode(String json) throws DecodeException {
		 Gson gson = new Gson();
		 OdlazniWsDTO dolazni = gson.fromJson(json, OdlazniWsDTO.class);   
		
		return dolazni;
	}

	@Override
	public boolean willDecode(String arg0) {
		// TODO Auto-generated method stub
		return true;
	}

}
