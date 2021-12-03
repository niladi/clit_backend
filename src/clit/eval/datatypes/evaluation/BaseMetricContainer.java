package clit.eval.datatypes.evaluation;

import java.util.List;

import com.google.common.collect.Lists;

public class BaseMetricContainer {
	private List<String> listPrecision = Lists.newArrayList(), listRecall = Lists.newArrayList(),
			listF1 = Lists.newArrayList();

	public void addPrecision(final List<String> entry) {
		if (entry == null) {
			// this.listPrecision.add("0.0");
			return;
		} else {
			this.listPrecision.addAll(entry);
		}
	}

	public void addRecall(final List<String> entry) {
		if (entry == null) {
			// this.listRecall.add("0.0");
			return;
		} else {
			this.listRecall.addAll(entry);
		}
	}

	public void addF1(final List<String> entry) {
		if (entry == null) {
			// this.listF1.add("0.0");
			return;
		} else {
			this.listF1.addAll(entry);
		}
	}

	public List<String> getPrecisions() {
		return this.listPrecision;
	}

	public List<String> getRecalls() {
		return this.listRecall;
	}

	public List<String> getF1() {
		return this.listF1;
	}
}
