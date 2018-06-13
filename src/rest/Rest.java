package rest;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Path;

@LocalBean
@Path("/agentskiCentar")
@Stateless
public class Rest implements RestRemote {

}
