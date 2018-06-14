package encoderDecoder;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import com.google.gson.Gson;

import dto.DolazniWsDTO;

public class DolazniWsDTODecoder implements Decoder.Text<DolazniWsDTO>  {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(EndpointConfig arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DolazniWsDTO decode(String json) throws DecodeException {
		
		 Gson gson = new Gson();
		 DolazniWsDTO dolazni = gson.fromJson(json, DolazniWsDTO.class);   
		
		return dolazni;
	}

	@Override
	public boolean willDecode(String arg0) {
		// TODO Auto-generated method stub
		return true;
	}

}
