/*******************************************************************************
 * OINK - Copyright (c) 2014 OpenEyes Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
