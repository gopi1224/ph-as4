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
package com.helger.as4.CEF;

import static org.junit.Assert.assertTrue;

import javax.mail.internet.MimeMessage;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.helger.as4.AS4TestConstants;
import com.helger.as4.attachment.EAS4CompressionMode;
import com.helger.as4.attachment.WSS4JAttachment;
import com.helger.as4.http.HttpMimeMessageEntity;
import com.helger.as4.http.HttpXMLEntity;
import com.helger.as4.messaging.mime.MimeMessageCreator;
import com.helger.as4.util.AS4ResourceManager;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.concurrent.ThreadHelper;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.commons.mime.CMimeType;

public final class AS4eSENSCEFTwoWayFuncTest extends AbstractCEFTwoWayTestSetUp
{
  /**
   * Prerequisite:<br>
   * SMSH and RMSH are configured to exchange AS4 messages according to the
   * e-SENS profile: Two-Way/Push-and-Push MEP. SMSH sends an AS4 User Message
   * (M1 with ID MessageId) that requires a consumer response to the RMSH. <br>
   * <br>
   * Predicate: <br>
   * The RMSH sends back a User Message (M2) with element REFTOMESSAGEID set to
   * MESSAGEID (of M1).
   *
   * @throws Exception
   *         In case of error
   */
  @Test
  public void eSENS_TA02_PushPush () throws Exception
  {
    final Document aDoc = testSignedUserMessage (m_eSOAPVersion, m_aPayload, null, new AS4ResourceManager ());

    final NodeList nList = aDoc.getElementsByTagName ("eb:MessageId");

    // Should only be called once
    final String aID = nList.item (0).getTextContent ();

    final String sResponse = sendPlainMessage (new HttpXMLEntity (aDoc, m_eSOAPVersion), true, null);

    assertTrue (sResponse.contains ("eb:RefToMessageId"));
    assertTrue (sResponse.contains (aID));

    // Wait for async response to come in
    // Otherwise indeterministic errors
    ThreadHelper.sleepSeconds (2);
  }

  /**
   * Prerequisite:<br>
   * SMSH and RMSH are configured to exchange AS4 messages according to the
   * e-SENS profile (Two-Way/Push-and-Push MEP). SMSH sends an AS4 User Message
   * to the RMSH.<br>
   * <br>
   * Predicate: <br>
   * The RMSH returns a non-repudiation receipt within a HTTP response with
   * status code 2XX.
   *
   * @throws Exception
   *         In case of error
   */
  @Test
  public void eSENS_TA16_PushPush () throws Exception
  {
    final ICommonsList <WSS4JAttachment> aAttachments = new CommonsArrayList <> ();
    aAttachments.add (WSS4JAttachment.createOutgoingFileAttachment (ClassPathResource.getAsFile (AS4TestConstants.ATTACHMENT_SHORTXML_XML),
                                                                    CMimeType.APPLICATION_XML,
                                                                    EAS4CompressionMode.GZIP,
                                                                    s_aResMgr));

    final MimeMessage aMsg = MimeMessageCreator.generateMimeMessage (m_eSOAPVersion,
                                                                     testSignedUserMessage (m_eSOAPVersion,
                                                                                            m_aPayload,
                                                                                            aAttachments,
                                                                                            new AS4ResourceManager ()),
                                                                     aAttachments);

    final String sResponse = sendMimeMessage (new HttpMimeMessageEntity (aMsg), true, null);

    assertTrue (sResponse.contains (AS4TestConstants.RECEIPT_ASSERTCHECK));
    assertTrue (sResponse.contains (AS4TestConstants.NON_REPUDIATION_INFORMATION));

    // Wait for async response to come in
    // Otherwise indeterministic errors
    ThreadHelper.sleepSeconds (2);
  }
}
