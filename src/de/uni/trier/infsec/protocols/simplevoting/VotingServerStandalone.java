package de.uni.trier.infsec.protocols.simplevoting;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import de.uni.trier.infsec.environment.network.NetworkError;
import de.uni.trier.infsec.functionalities.pkenc.real.Decryptor;
import de.uni.trier.infsec.functionalities.pkenc.real.Encryptor;
import de.uni.trier.infsec.lib.network.Network;
import de.uni.trier.infsec.utils.Utilities;

public class VotingServerStandalone{
	
	public static final String[] votes = new String[] { "01", "02", "03", "04", "05"};
	
	public static final String publicKey = "30819F300D06092A864886F70D010101050003818D0030818902818100DAB82D01DAEDB88" +
			"F350ADA267308AB7AD57A337E1D0E6466D16200EE7804C2229BD78B0235364CA3AC5DAD17FF57683810E1208D6D25B9E3977ECCD" +
			"6DB0856889F7B01321A3748CE363495C63621DE4A8CBC0711D76E0A8C4E55272020A1F063678F9495E8C14577A04903F5D3E504B" +
			"2855DE642931A41E205EB6240850B9BD30203010001";
	
	public static final String privateKey = "30820277020100300D06092A864886F70D0101010500048202613082025D020100028181" +
			"00DAB82D01DAEDB88F350ADA267308AB7AD57A337E1D0E6466D16200EE7804C2229BD78B0235364CA3AC5DAD17FF57683810E120" +
			"8D6D25B9E3977ECCD6DB0856889F7B01321A3748CE363495C63621DE4A8CBC0711D76E0A8C4E55272020A1F063678F9495E8C145" +
			"77A04903F5D3E504B2855DE642931A41E205EB6240850B9BD3020301000102818100D6967A79E67CF36575AA170C40329263AA8D" +
			"01764B35B2A5F9EA4875AF4523DF66BD1BC267C8C57A9403386F61F334EA450D4BADD6177C80E242E2E02DF7C944E3A01001636A" +
			"C500982B4AFDE4F1EEC2C7BBB75C0C56FB6A9316B6BC9B3954E4F00E8086466622C37C522D547DF226C4C1F4570E0748968EC7E5" +
			"F85B098753D1024100F190D0BAB4B84F7A80044DF1C6DB027B5CD0FB57287A1FB2AB14FB8E7C3FE74CBB76A3F76AD4721B8BB4A5" +
			"6FAABCA2244EA711CC45652C9E9F9DB9088A66800B024100E7C9E2BE631A4035DBC3F74A12C60DB69CAD122C0D0C902EFA1B93ED" +
			"96B608DB482BA6724F842A7A6B16B8BD6AE255597E3E5D5C6F5F221C1AF9ED5C3BF2485902405AB150FC57EF3EBFB2226B951360" +
			"945CF66AEB823C8B252D7237CD7E203DE9BC1041A9ABB16B13702E12636E3A3ED9ED21AE6DEB303E9CF2ECE04D60DC7D41230240" +
			"0433A0AC9AD74AFAAEF52A72694CB5CAEDA425842EE85F64BA9BED5E8D30D790420AA885C1F33F61E0B714BA3A49C80A4B438E25" +
			"B2CF22AB27C2080F77F6B86102410088539B65FB126BCA6EBD597E351E97BCA196500AEC1F1263E852E5AFECECF4E4FEFEF7ED55" +
			"07557C561CED4319639A59B77FC55C74A11B76BD40F1FDC7AFE560";
	
	
	private VotingServerCore server = null;

	public void startServer(String votersPublicKeys) throws IOException {
		
		BufferedReader br = new BufferedReader(new FileReader(votersPublicKeys));
		int count = Integer.parseInt(br.readLine());
		Encryptor[] encryptors = new Encryptor[count];
		for (int i = 0; i < count; i++) {
			String tmp = br.readLine();
			byte[] publicKey = Utilities.hexStringToByteArray(tmp);
			encryptors[i] = new Encryptor(publicKey);
			
			System.out.println(tmp);
		}
		
		byte[] pubKey = Utilities.hexStringToByteArray(publicKey);
		byte[] privKey = Utilities.hexStringToByteArray(privateKey);
		server = new VotingServerCore(new Decryptor(pubKey, privKey), encryptors);
		
		
		while (true) {
			try {				
				Network.waitForClient(Network.DEFAULT_PORT);
				doProtocol();			
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				Network.resetConnection();
			}
		}
	}

	
	private void doProtocol() {
		try {
			byte[] input = Network.networkIn();
			System.out.println("Received request via network: " + Utilities.byteArrayToHexString(input));
			if (input[0] == 0x01) { // Ballot submitted
				byte[] ballot = new byte[input.length - 1];
				System.arraycopy(input, 1, ballot, 0, ballot.length);
				server.collectBallot(ballot);
			} else if (input[0] == 0x02) { // Registration
				byte[] publicKey = new byte[input.length - 1];
				System.arraycopy(input, 1, publicKey, 0, publicKey.length);
				byte[] credential = server.getCredential(publicKey);
				Network.networkOut(credential);
			}
		} catch (NetworkError e) {
			e.printStackTrace();
			return;
		}
	}

	public static void main(String[] args) throws IOException {
		VotingServerStandalone server = new VotingServerStandalone();
		if (args.length < 1) {
			System.out.println("Parameter missing. Usage: VotingServerStandalone <path-to-voters-publickeys>");
		}
		server.startServer(args[0]);
	}

}
