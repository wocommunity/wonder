package er.vertx.woadaptor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import com.webobjects.foundation.NSForwardException;

import io.vertx.core.net.PemKeyCertOptions;

public class CertificateUtils {
	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	private static final String PRIVATE_KEY_LABEL = "PRIVATE KEY";
	private static final String CERTIFICATE_LABEL = "CERTIFICATE";
	private static final String SIGNATURE_ALGORITHM = "SHA256withECDSA";
	private static final String PRIVATE_KEY_FILE_NAME = "privkey.pem";
	private static final String FULL_CHAIN_FILE_NAME = "fullchain.pem";

	public static void generateSelfSignedForHostInDir(final String hostName, final File dir) throws IOException,
	CertificateException, NoSuchAlgorithmException, NoSuchProviderException, OperatorCreationException {
		final KeyPairGenerator keyPairGenerator = KeyPairGenerator
				.getInstance(X9ObjectIdentifiers.id_ecPublicKey.getId(),
						BouncyCastleProvider.PROVIDER_NAME);
		keyPairGenerator.initialize(256);
		final KeyPair keyPair = keyPairGenerator.generateKeyPair();
		final PrivateKey privateKey = keyPair.getPrivate();
		final PublicKey publicKey = keyPair.getPublic();

		final String CN = "CN=" + hostName;
		final JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
				new org.bouncycastle.asn1.x500.X500Name(CN),
				BigInteger.valueOf(System.currentTimeMillis()),
				new Date(System.currentTimeMillis() - 86400000L), new Date(System.currentTimeMillis() + 31536000000L),
				new org.bouncycastle.asn1.x500.X500Name(CN), publicKey);

		final JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM);
		signerBuilder.setProvider(BouncyCastleProvider.PROVIDER_NAME);
		final ContentSigner contentSigner = signerBuilder.build(privateKey);

		final JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
		converter.setProvider(BouncyCastleProvider.PROVIDER_NAME);
		final X509Certificate cert = converter.getCertificate(certBuilder.build(contentSigner));

		try (PemWriter privKeyWriter = new PemWriter(new FileWriter(new File(dir, PRIVATE_KEY_FILE_NAME)))) {
			privKeyWriter.writeObject(new PemObject(PRIVATE_KEY_LABEL, privateKey.getEncoded()));
		}
		try (PemWriter certWriter = new PemWriter(new FileWriter(new File(dir, "cert.pem")))) {
			certWriter.writeObject(new PemObject(CERTIFICATE_LABEL, cert.getEncoded()));
		}
		try (PemWriter fullchainWriter = new PemWriter(new FileWriter(new File(dir, FULL_CHAIN_FILE_NAME)))) {
			fullchainWriter.writeObject(new PemObject(CERTIFICATE_LABEL, cert.getEncoded()));
		}
		try (FileWriter chainWriter = new FileWriter(new File(dir, "chain.pem"))) {
			chainWriter.write("");
		}
	}

	public static PemKeyCertOptions optionsForHostAndDir(final String hostName, final File dir) {
		final File keyFile = new File(dir, PRIVATE_KEY_FILE_NAME);
		final File certFile = new File(dir, FULL_CHAIN_FILE_NAME);
		if (!keyFile.exists()) {
			if(!dir.exists()) {
				dir.mkdirs();
			}
			try {
				generateSelfSignedForHostInDir(hostName, dir);
			} catch (CertificateException | NoSuchAlgorithmException | NoSuchProviderException
					| OperatorCreationException | IOException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
		}
		return new PemKeyCertOptions().addKeyPath(keyFile.getAbsolutePath()).addCertPath(certFile.getAbsolutePath());
	}
}
