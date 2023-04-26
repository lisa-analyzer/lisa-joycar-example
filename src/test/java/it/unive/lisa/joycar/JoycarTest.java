package it.unive.lisa.joycar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.LiSAReport;
import it.unive.lisa.checks.warnings.ExpressionWarning;
import it.unive.lisa.checks.warnings.Warning;

public class JoycarTest {

	@Test
	public void noSanitizer() throws AnalysisException, IOException {
		System.out.println("### executing without sanitization");
		LiSAReport report = App.mainImpl(false);
		assertEquals(1, report.getWarnings().size());
		Warning w = report.getWarnings().iterator().next();
		assertTrue(w instanceof ExpressionWarning);
		ExpressionWarning ew = (ExpressionWarning) w;
		assertEquals("'original/JoyCar.cpp':124:55", FilenameUtils.separatorsToUnix(ew.getLocation().toString()));
		assertEquals(
				"The value passed for the 2nd parameter of this call is tainted, and it reaches the sink at parameter 'value' of ~LiSAProgram::communicate",
				w.getMessage());
	}

	@Test
	public void sanitizer() throws AnalysisException, IOException {
		System.out.println("### executing with sanitization");
		LiSAReport report = App.mainImpl(true);
		assertEquals(0, report.getWarnings().size());
	}
}
