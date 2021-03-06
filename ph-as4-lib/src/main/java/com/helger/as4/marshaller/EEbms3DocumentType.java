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
package com.helger.as4.marshaller;

import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.validation.Schema;

import com.helger.as4.CAS4;
import com.helger.as4lib.ebms3header.Ebms3Messaging;
import com.helger.as4lib.ebms3header.NonRepudiationInformation;
import com.helger.as4lib.soap11.Soap11Envelope;
import com.helger.as4lib.soap12.Soap12Envelope;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.commons.io.resource.IReadableResource;
import com.helger.jaxb.builder.IJAXBDocumentType;
import com.helger.jaxb.builder.JAXBDocumentType;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public enum EEbms3DocumentType implements IJAXBDocumentType
{
  MESSAGING (Ebms3Messaging.class,
             new CommonsArrayList <> (new ClassPathResource ("/schemas/xmldsig-core-schema.xsd"),
                                      new ClassPathResource (CAS4.XSD_EBBP_SIGNALS),
                                      new ClassPathResource (CAS4.XSD_EBMS_HEADER))),
  NON_REPUDIATION_INFORMATION (NonRepudiationInformation.class,
                               new CommonsArrayList <> (new ClassPathResource ("/schemas/xmldsig-core-schema.xsd"),
                                                        new ClassPathResource (CAS4.XSD_EBBP_SIGNALS))),
  SOAP_11 (Soap11Envelope.class, new CommonsArrayList <> (new ClassPathResource (CAS4.XSD_SOAP11))),
  SOAP_12 (Soap12Envelope.class, new CommonsArrayList <> (new ClassPathResource (CAS4.XSD_SOAP12)));

  private final JAXBDocumentType m_aDocType;

  private EEbms3DocumentType (@Nonnull final Class <?> aClass,
                              @Nonnull final Iterable <? extends IReadableResource> aXSDPaths)
  {
    this (aClass, aXSDPaths, null);
  }

  @SuppressFBWarnings ("NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE")
  private EEbms3DocumentType (@Nonnull final Class <?> aClass,
                              @Nonnull final Iterable <? extends IReadableResource> aXSDPaths,
                              @Nullable final Function <String, String> aTypeToElementNameMapper)
  {
    m_aDocType = new JAXBDocumentType (aClass,
                                       new CommonsArrayList <> (aXSDPaths, IReadableResource::getPath),
                                       aTypeToElementNameMapper);
  }

  @Nonnull
  public Class <?> getImplementationClass ()
  {
    return m_aDocType.getImplementationClass ();
  }

  @Nonnull
  @Nonempty
  @ReturnsMutableCopy
  public ICommonsList <String> getAllXSDPaths ()
  {
    return m_aDocType.getAllXSDPaths ();
  }

  @Nonnull
  public String getNamespaceURI ()
  {
    return m_aDocType.getNamespaceURI ();
  }

  @Nonnull
  @Nonempty
  public String getLocalName ()
  {
    return m_aDocType.getLocalName ();
  }

  @Nonnull
  public Schema getSchema (@Nullable final ClassLoader aClassLoader)
  {
    return m_aDocType.getSchema (aClassLoader);
  }
}
