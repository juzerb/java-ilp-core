package org.interledger.wire.codecs.oer.ilp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import org.interledger.InterledgerAddressBuilder;
import org.interledger.ilp.InterledgerPayment;
import org.interledger.wire.CodecContextFactory;
import org.interledger.wire.InterledgerPacket;
import org.interledger.wire.codecs.Codec;
import org.interledger.wire.codecs.CodecContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Unit tests to validate the {@link Codec} functionality for all {@link InterledgerPayment} packets.
 */
@RunWith(Parameterized.class)
public class InterledgerPaymentOerCodecTests {

  // first data value (0) is default
  @Parameter
  public InterledgerPacket packet;

  @Parameters
  public static Collection<Object[]> data() {

    // This ByteArrayOutputStream contains a random amount of 32kb for testing purposes.
    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    for (int i = 0; i < 32768; i++) {
      byteArrayOutputStream.write(i);
    }

    return Arrays.asList(new Object[][]{
      {new InterledgerPayment.Builder().
        destinationAccount(InterledgerAddressBuilder.builder().value("test3.foo").build())
        .destinationAmount(100L)
        .data(new byte[]{})
        .build()},

      {new InterledgerPayment.Builder().
        destinationAccount(InterledgerAddressBuilder.builder().value("test1.bar").build())
        .destinationAmount(50L)
        .data(new byte[]{1, 2, 3, 4, 5, 6, 7, 8})
        .build()},

      {new InterledgerPayment.Builder().
        destinationAccount(InterledgerAddressBuilder.builder().value("test1.bar").build())
        .destinationAmount(50L)
        .data(byteArrayOutputStream.toByteArray())
        .build()},

    });
  }

  /**
   * The primary difference between this test and {@link #testInterledgerPaymentCodec()} is that
   * this context call specifies the type, whereas the test below determines the type from the
   * payload.
   */
  @Test
  public void testIndividualRead() throws IOException {
    final CodecContext context = CodecContextFactory.interledger();
    final ByteArrayInputStream asn1OerPaymentBytes = constructAsn1OerPaymentBytes();

    final InterledgerPayment payment = context.read(InterledgerPayment.class, asn1OerPaymentBytes);
    assertThat(payment, is(packet));
  }

  /**
   * The primary difference between this test and {@link #testIndividualRead()} is that
   * this context determines the interledgerPayment type from the payload, whereas the test above specifies the
   * type in the method call.
   */
  @Test
  public void testInterledgerPaymentCodec() throws Exception {
    final CodecContext context = CodecContextFactory.interledger();
    final ByteArrayInputStream asn1OerPaymentBytes = constructAsn1OerPaymentBytes();

    final InterledgerPacket decodedPacket = context.read(asn1OerPaymentBytes);
    assertThat(decodedPacket.getClass().getName(), is(packet.getClass().getName()));
    assertThat(decodedPacket, is(packet));
  }

  private ByteArrayInputStream constructAsn1OerPaymentBytes() throws IOException {
    final CodecContext context = CodecContextFactory.interledger();

    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    context.write(packet, outputStream);

    return new ByteArrayInputStream(outputStream.toByteArray());
  }

}