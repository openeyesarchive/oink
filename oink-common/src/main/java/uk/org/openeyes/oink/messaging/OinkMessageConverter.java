/*******************************************************************************
 * Copyright (c) 2014 OpenEyes Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package uk.org.openeyes.oink.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;

import uk.org.openeyes.oink.domain.OINKMessage;
import uk.org.openeyes.oink.domain.OINKRequestMessage;

/**
 * Small extension to {@link Jackson2JsonMessageConverter}. If an incoming
 * message does not have __TypeId__ set in the header then we assume the content
 * to be a byte array.
 * 
 * @author Oliver Wilkie
 */
public class OinkMessageConverter implements MessageConverter {

	public final static Logger logger = LoggerFactory
			.getLogger(OinkMessageConverter.class);
	private final static Jackson2JsonMessageConverter jsonConverter = new Jackson2JsonMessageConverter();

	@Override
	public Object fromMessage(Message m) throws MessageConversionException {
		if (m.getMessageProperties().getContentType()
				.equals("application/json")) {
			// Process as JSON
			try {
				OINKMessage message = (OINKMessage) jsonConverter
						.fromMessage(m);
				return message;
			} catch (MessageConversionException e) {
				return new InvalidOinkMessageException();
			}
		} else {
			// Process as non-JSON i.e. invalid
			return new InvalidOinkMessageException();
		}
	}

	@Override
	public Message toMessage(Object o, MessageProperties p)
			throws MessageConversionException {
		logger.debug("Converting object of type "+o.getClass().getSimpleName()+" to JSON");
		return jsonConverter.toMessage(o, p);
	}

}
