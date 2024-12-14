package org.tailkeep.api.integration;

import org.tailkeep.api.model.Channel;
import org.tailkeep.api.model.DownloadProgress;
import org.tailkeep.api.model.Job;
import org.tailkeep.api.model.Video;

public record TestEntities(
  Channel channel,
  Video video,
  Job job,
  DownloadProgress progress
) {} 
