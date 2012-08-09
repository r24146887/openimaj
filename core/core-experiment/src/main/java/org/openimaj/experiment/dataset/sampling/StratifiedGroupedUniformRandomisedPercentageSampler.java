package org.openimaj.experiment.dataset.sampling;

import java.util.List;
import java.util.Map;

import org.openimaj.data.RandomData;
import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.MapBackedDataset;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.util.list.AcceptingListView;
import org.openimaj.util.list.SkippingListView;

/**
 * A stratified uniformly random sampling scheme for grouped datasets. Both
 * sampling with and without replacement are supported. The sampler returns a
 * dataset that selects a predefined fraction of the input data. Specifically,
 * the given percentage of data is selected from each group independently, thus
 * ensuring that the distribution of relative group sizes before and after
 * sampling remains (approximately) constant.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <KEY>
 *            Type of groups
 * @param <INSTANCE>
 *            Type of instances
 */
public class StratifiedGroupedUniformRandomisedPercentageSampler<KEY, INSTANCE>
		implements
		Sampler<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>> {
	private boolean withReplacement = false;
	private double percentage;

	/**
	 * Construct a {@link StratifiedGroupedUniformRandomisedPercentageSampler}
	 * with the given percentage of instances to select. By default, the
	 * sampling is without replacement (i.e. an instance can only be selected
	 * once).
	 * 
	 * @param percentage
	 *            percentage of instances to select
	 */
	public StratifiedGroupedUniformRandomisedPercentageSampler(double percentage) {
		if (percentage < 0 || percentage > 1)
			throw new IllegalArgumentException("percentage of sample instances must be between 0 and 1");
		
		this.percentage = percentage;
	}

	/**
	 * Construct a {@link StratifiedGroupedUniformRandomisedPercentageSampler}
	 * with the given percentage of instances to select, using with
	 * with-replacement or without-replacement sampling.
	 * 
	 * @param percentage
	 *            percentage of instances to select
	 * @param withReplacement
	 *            should the sampling be performed with replacement (true); or
	 *            without replacement (false).
	 */
	public StratifiedGroupedUniformRandomisedPercentageSampler(
			double percentage, boolean withReplacement) {
		this(percentage);
		this.withReplacement = withReplacement;
	}

	@Override
	public GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> sample(
			GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> dataset) {

		MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> sample = new MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>();
		Map<KEY, ListDataset<INSTANCE>> map = sample.getMap();

		for (KEY key : dataset.getGroups()) {
			final List<INSTANCE> list = DatasetAdaptors.asList(dataset
					.getInstances(key));
			final int size = list.size();

			// if we want more than 50%, it's better to select 1-percentage
			// indexes to skip
			final boolean skip = percentage > 0.5;
			final double per = skip ? 1.0 - percentage : percentage;

			final int N = (int) Math.round(size * per);

			int[] selectedIds;
			if (withReplacement) {
				selectedIds = RandomData.getRandomIntArray(N, 0, size);
			} else {
				selectedIds = RandomData.getUniqueRandomInts(N, 0, size);
			}

			if (!skip) {
				map.put(key, new ListBackedDataset<INSTANCE>(
						new AcceptingListView<INSTANCE>(list, selectedIds)));
			} else {
				map.put(key, new ListBackedDataset<INSTANCE>(
						new SkippingListView<INSTANCE>(list, selectedIds)));
			}
		}

		return sample;
	}
}
