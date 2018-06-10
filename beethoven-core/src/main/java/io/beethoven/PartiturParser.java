/**
 * The MIT License
 * Copyright © 2018 Davi Monteiro
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.beethoven;

import com.google.inject.Inject;
import com.google.inject.Injector;
import io.beethoven.partitur.PartiturStandaloneSetup;
import io.beethoven.partitur.partitur.PartiturWorkflow;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.parser.IParser;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;

/**
 *
 * @author Davi Monteiro
 */
public class PartiturParser {

    @Inject
    private IParser parser;

    public PartiturParser() {
        setupParser();
    }

    private void setupParser() {
        Injector injector = new PartiturStandaloneSetup().createInjectorAndDoEMFRegistration();
        injector.injectMembers(this);
    }

    public PartiturWorkflow parse(Reader reader) throws IOException, ParseException {
        IParseResult result = parser.parse(reader);
        Iterable<INode> syntaxErrors = result.getSyntaxErrors();
        syntaxErrors.forEach(error -> error.getSyntaxErrorMessage().toString());
        if (result.hasSyntaxErrors()) {
            throw new ParseException("Provided input contains syntax errors.", 0);
        }
        return (PartiturWorkflow) result.getRootASTElement();
    }

}
