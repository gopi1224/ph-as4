/**
 * Copyright (C) 2015-2018 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.as4.client;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.mail.internet.MimeMessage;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.helger.as4.attachment.WSS4JAttachment;
import com.helger.as4.crypto.AS4CryptoFactory;
import com.helger.as4.crypto.ECryptoAlgorithmCrypt;
import com.helger.as4.crypto.ECryptoAlgorithmSign;
import com.helger.as4.crypto.ECryptoAlgorithmSignDigest;
import com.helger.as4.http.HttpMimeMessageEntity;
import com.helger.as4.http.HttpXMLEntity;
import com.helger.as4.messaging.domain.MessageHelperMethods;
import com.helger.as4.messaging.encrypt.EncryptionCreator;
import com.helger.as4.messaging.mime.MimeMessageCreator;
import com.helger.as4.messaging.sign.SignedMessageCreator;
import com.helger.as4.soap.ESOAPVersion;
import com.helger.as4.util.AS4ResourceManager;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.commons.mime.CMimeType;
import com.helger.commons.random.RandomHelper;
import com.helger.commons.ws.TrustManagerTrustAll;
import com.helger.httpclient.HttpClientFactory;
import com.helger.xml.serialize.read.DOMReader;

public final class MainAS4Client
{
  private static final Logger LOGGER = LoggerFactory.getLogger (MainAS4Client.class);

  private MainAS4Client ()
  {}

  public static Document getSoapEnvelope11ForTest (@Nonnull final String sPath) throws SAXException,
                                                                                IOException,
                                                                                ParserConfigurationException
  {
    final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance ();
    domFactory.setNamespaceAware (true); // never forget this!
    final DocumentBuilder builder = domFactory.newDocumentBuilder ();
    return builder.parse (new ClassPathResource (sPath).getInputStream ());
  }

  /**
   * Starting point for the SAAJ - SOAP Client Testing
   *
   * @param args
   *        ignored
   */
  public static void main (final String [] args)
  {
    try (final AS4ResourceManager aResMgr = new AS4ResourceManager ())
    {
      String sURL = "http://127.0.0.1:8080/as4";
      if (false)
        sURL = "http://msh.holodeck-b2b.org:8080/msh";

      // Deactivate if not sending to localholodeck
      if (false)
        sURL = "http://localhost:8080/msh/";

      SSLContext aSSLContext = null;
      if (sURL.startsWith ("https"))
      {
        aSSLContext = SSLContext.getInstance ("TLS");
        aSSLContext.init (null,
                          new TrustManager [] { new TrustManagerTrustAll (false) },
                          RandomHelper.getSecureRandom ());
      }

      final CloseableHttpClient aClient = new HttpClientFactory ().setSSLContext (aSSLContext).createHttpClient ();

      LOGGER.info ("Sending to " + sURL);
      final HttpPost aPost = new HttpPost (sURL);

      if (!sURL.contains ("localhost") && !sURL.contains ("127.0.0.1"))
        aPost.setConfig (RequestConfig.custom ().setProxy (new HttpHost ("172.30.9.12", 8080)).build ());

      final ICommonsList <WSS4JAttachment> aAttachments = new CommonsArrayList <> ();
      final Node aPayload = DOMReader.readXMLDOM (new ClassPathResource ("SOAPBodyPayload.xml"));
      final ESOAPVersion eSOAPVersion = ESOAPVersion.SOAP_12;

      // No Mime Message Not signed or encrypted, just SOAP + Payload in SOAP -
      // Body
      if (true)
      {
        // final Document aDoc = TestMessages.testSignedUserMessage
        // (ESOAPVersion.SOAP_11, aPayload, aAttachments);
        final Document aDoc = MockClientMessages.testUserMessageSoapNotSigned (eSOAPVersion, aPayload, aAttachments);
        aPost.setEntity (new HttpXMLEntity (aDoc, eSOAPVersion));
      }
      else
        // BodyPayload SIGNED
        if (false)
        {
          final Document aDoc = MockClientMessages.testSignedUserMessage (eSOAPVersion,
                                                                          aPayload,
                                                                          aAttachments,
                                                                          aResMgr);
          aPost.setEntity (new HttpXMLEntity (aDoc, eSOAPVersion));
        }
        // BodyPayload ENCRYPTED
        else
          if (false)
          {
            Document aDoc = MockClientMessages.testUserMessageSoapNotSigned (eSOAPVersion, aPayload, aAttachments);
            aDoc = new EncryptionCreator (AS4CryptoFactory.DEFAULT_INSTANCE).encryptSoapBodyPayload (eSOAPVersion,
                                                                                                     aDoc,
                                                                                                     false,
                                                                                                     ECryptoAlgorithmCrypt.ENCRPYTION_ALGORITHM_DEFAULT);

            aPost.setEntity (new HttpXMLEntity (aDoc, eSOAPVersion));
          }
          else
            if (true)
            {
              aAttachments.add (WSS4JAttachment.createOutgoingFileAttachment (ClassPathResource.getAsFile ("attachment/test.xml.gz"),
                                                                              CMimeType.APPLICATION_GZIP,
                                                                              null,
                                                                              aResMgr));

              final MimeMessage aMsg = MimeMessageCreator.generateMimeMessage (eSOAPVersion,
                                                                               SignedMessageCreator.createSignedMessage (AS4CryptoFactory.DEFAULT_INSTANCE,
                                                                                                                         MockClientMessages.testUserMessageSoapNotSigned (eSOAPVersion,
                                                                                                                                                                          null,
                                                                                                                                                                          aAttachments),
                                                                                                                         eSOAPVersion,
                                                                                                                         aAttachments,
                                                                                                                         aResMgr,
                                                                                                                         false,
                                                                                                                         ECryptoAlgorithmSign.SIGN_ALGORITHM_DEFAULT,
                                                                                                                         ECryptoAlgorithmSignDigest.SIGN_DIGEST_ALGORITHM_DEFAULT),
                                                                               aAttachments);

              // Move all global mime headers to the POST request
              MessageHelperMethods.moveMIMEHeadersToHTTPHeader (aMsg, aPost);
              aPost.setEntity (new HttpMimeMessageEntity (aMsg));
            }
            else
              if (false)
              {
                Document aDoc = MockClientMessages.testSignedUserMessage (eSOAPVersion,
                                                                          aPayload,
                                                                          aAttachments,
                                                                          aResMgr);
                aDoc = new EncryptionCreator (AS4CryptoFactory.DEFAULT_INSTANCE).encryptSoapBodyPayload (eSOAPVersion,
                                                                                                         aDoc,
                                                                                                         false,
                                                                                                         ECryptoAlgorithmCrypt.ENCRPYTION_ALGORITHM_DEFAULT);
                aPost.setEntity (new HttpXMLEntity (aDoc, eSOAPVersion));
              }
              else
                throw new IllegalStateException ("Some test message should be selected :)");

      // XXX reinstate if you wanna see the request that is getting sent
      LOGGER.info (EntityUtils.toString (aPost.getEntity ()));

      final CloseableHttpResponse aHttpResponse = aClient.execute (aPost);

      LOGGER.info ("GET Response Status:: " + aHttpResponse.getStatusLine ().getStatusCode ());

      // print result
      LOGGER.info (EntityUtils.toString (aHttpResponse.getEntity ()));
    }
    catch (final Exception e)
    {
      LOGGER.error ("Error occurred while sending SOAP Request to Server", e);
    }
  }
}
