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
package com.helger.as4.model.pmode.leg;

import java.io.Serializable;
import java.net.URL;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.as4.soap.ESOAPVersion;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.MustImplementEqualsAndHashcode;
import com.helger.commons.equals.EqualsHelper;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.state.EChange;
import com.helger.commons.string.ToStringGenerator;
import com.helger.commons.url.URLHelper;

/**
 * PMode leg protocol parameters.
 *
 * @author Philip Helger
 */
@NotThreadSafe
@MustImplementEqualsAndHashcode
public class PModeLegProtocol implements Serializable
{
  /**
   * the value of this parameter represents the address (endpoint URL) of the
   * Receiver MSH (or Receiver Party) to which Messages under this P-Mode leg
   * are to be sent. Note that a URL generally determines the transport protocol
   * (for example, if the endpoint is an email address, then the transport
   * protocol must be SMTP; if the address scheme is "http", then the transport
   * protocol must be HTTP).
   */
  private String m_sAddress;

  /**
   * this parameter indicates the SOAP version to be used (<code>1.1</code> or
   * <code>1.2</code>). In some implementations, this parameter may be
   * constrained by the implementation, and not set by users.
   */
  private ESOAPVersion m_eSOAPVersion;

  public PModeLegProtocol (@Nullable final String sAddress, @Nonnull final ESOAPVersion eSOAPVersion)
  {
    setAddress (sAddress);
    setSOAPVersion (eSOAPVersion);
  }

  @Nullable
  public String getAddress ()
  {
    return m_sAddress;
  }

  @Nullable
  public String getAddressProtocol ()
  {
    final URL aURL = URLHelper.getAsURL (m_sAddress);
    return aURL == null ? null : aURL.getProtocol ();
  }

  @Nonnull
  public final EChange setAddress (@Nullable final String sAddress)
  {
    if (EqualsHelper.equals (sAddress, m_sAddress))
      return EChange.UNCHANGED;
    m_sAddress = sAddress;
    return EChange.CHANGED;
  }

  @Nonnull
  public ESOAPVersion getSOAPVersion ()
  {
    return m_eSOAPVersion;
  }

  @Nonnull
  public final EChange setSOAPVersion (@Nonnull final ESOAPVersion eSOAPVersion)
  {
    ValueEnforcer.notNull (eSOAPVersion, "SOAPVersion");
    if (eSOAPVersion.equals (m_eSOAPVersion))
      return EChange.UNCHANGED;
    m_eSOAPVersion = eSOAPVersion;
    return EChange.CHANGED;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final PModeLegProtocol rhs = (PModeLegProtocol) o;
    return EqualsHelper.equals (m_sAddress, rhs.m_sAddress) && m_eSOAPVersion.equals (rhs.m_eSOAPVersion);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_sAddress).append (m_eSOAPVersion).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("Address", m_sAddress)
                                       .append ("SOAPVersion", m_eSOAPVersion)
                                       .getToString ();
  }

  @Nonnull
  public static PModeLegProtocol createForDefaultSOAPVersion (@Nullable final String sAddress)
  {
    return new PModeLegProtocol (sAddress, ESOAPVersion.AS4_DEFAULT);
  }
}
