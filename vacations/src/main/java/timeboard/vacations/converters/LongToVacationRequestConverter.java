package timeboard.vacations.converters;

/*-
 * #%L
 * vacations
 * %%
 * Copyright (C) 2019 - 2020 Timeboard
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import timeboard.core.api.VacationService;
import timeboard.core.model.VacationRequest;
import timeboard.core.security.TimeboardAuthentication;

import java.util.Optional;

@Component
public class LongToVacationRequestConverter implements Converter<String, VacationRequest> {

    @Autowired
    private VacationService service;

    @Override
    public VacationRequest convert(final String aLong) {
        final TimeboardAuthentication auth = (TimeboardAuthentication) SecurityContextHolder.getContext().getAuthentication();
        final Optional<VacationRequest> request = this.service.getVacationRequestByID(auth.getDetails(), Long.parseLong(aLong));
        if (request.isPresent()) {
            return this.service.getVacationRequestByID(auth.getDetails(), Long.parseLong(aLong)).get();
        } else {
            return null;
        }
    }
}
