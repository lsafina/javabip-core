package org.bip.spec.telephonic;

import java.util.HashMap;

import org.bip.annotations.Data;
import org.bip.annotations.ExecutableBehaviour;
import org.bip.api.BIPActor;
import org.bip.api.PortType;
import org.bip.executor.BehaviourBuilder;

public class DiscAgregation1 implements ClientCaller {
	private int n;
	BIPActor discSync;
	HashMap<Integer, BIPActor> clientActors;
	
	public DiscAgregation1(int n)
	{
		this.n=n;
		clientActors = new HashMap<Integer, BIPActor>(n);
	}
	
	public void setSyncRefs(BIPActor discSync)
	{
		this.discSync = discSync;
	}
	
	public void setClientRefs(BIPActor client, int id)
	{
		clientActors.put(id, client);
	}

	@ExecutableBehaviour
    public BehaviourBuilder getExecutableBehavior() throws NoSuchMethodException {

		BehaviourBuilder behaviourBuilder = new BehaviourBuilder(this);
				
		behaviourBuilder.setComponentType(this.getClass().getCanonicalName());

        String currentState = "s0";

        behaviourBuilder.setInitialState(currentState);
      
        behaviourBuilder.addPort("discUp", PortType.spontaneous, this.getClass());
        behaviourBuilder.addPort("discDown", PortType.spontaneous, this.getClass());     
      		
        behaviourBuilder.addTransitionAndStates("discUp","s0", "s0",  "", this.getClass().getMethod("discUp",Integer.class, Integer.class));
        behaviourBuilder.addTransitionAndStates("discDown","s0", "s0",  "", this.getClass().getMethod("discDown",Integer.class, Integer.class));
     

        return behaviourBuilder;
    }
	
	public void discUp(@Data(name="dialerId") Integer dialerId, @Data(name="waiterId") Integer waiterId )
	{
	 	 System.out.println("VoiceAgregation "+ " is notified of "+ dialerId+" speaking to " + waiterId);
		 HashMap<String, Object> dataMap = new HashMap<String, Object>();
		 dataMap.put("dialerId", dialerId);
		 dataMap.put("waiterId", waiterId);
		 discSync.inform("disc1",dataMap);
	}
	
	public void discDown(@Data(name="dialerId") Integer dialerId, @Data(name="waiterId") Integer waiterId)
	{
		 System.out.println("VoiceAgregation "+ " is trasferring disc to client "+ dialerId);
		 HashMap<String, Object> dataMap = new HashMap<String, Object>();
		 dataMap.put("waiterId", waiterId);
		 clientActors.get(dialerId).inform("disc",dataMap);
	}
}
