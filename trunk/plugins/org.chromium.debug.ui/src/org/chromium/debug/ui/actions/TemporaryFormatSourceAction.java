package org.chromium.debug.ui.actions;

import java.io.IOException;
import java.util.List;

import org.chromium.debug.core.model.DebugTargetImpl;
import org.chromium.debug.core.model.JavaScriptFormatter;
import org.chromium.debug.core.model.VmResource;
import org.chromium.debug.core.model.WorkspaceBridge;
import org.chromium.debug.core.util.ChromiumDebugPluginUtil;
import org.chromium.debug.ui.actions.FileBasedAction.FileFilter;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;

public class TemporaryFormatSourceAction
    extends FileBasedAction.Multiple<TemporaryFormatSourceAction.ResourceData> {
  public TemporaryFormatSourceAction() {
    super(RESOURCE_FILTER);
  }

  private static final FileFilter<ResourceData> RESOURCE_FILTER =
      new FileFilter<ResourceData>() {
    @Override
    ResourceData accept(IFile file) {
      for (DebugTargetImpl target : DebugTargetImpl.getAllDebugTargetImpls()) {
        VmResource vmResource = target.getWorkspaceRelations().getVProjectVmResource(file);
        if (vmResource == null) {
          continue;
        }
        return new ResourceData(file, vmResource, target);
      }
      return null;
    }
  };

  @Override
  protected void execute(List<? extends ResourceData> selected, Shell shell) {
    for (ResourceData data : selected) {
      try {
        beautifyFile(data);
      } catch (CoreException e) {
        throw new RuntimeException(e);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void beautifyFile(ResourceData data) throws CoreException, IOException {
    byte[] sourceBytes = ChromiumDebugPluginUtil.readFileContents(data.getFile());
    String sourceString = new String(sourceBytes);

    JavaScriptFormatter formatter = JavaScriptFormatter.Access.getInstance();
    if (formatter == null) {
      throw new RuntimeException("No formatter implementation found");
    }
    JavaScriptFormatter.Result result = formatter.format(sourceString);

    WorkspaceBridge workspaceRelations = data.getTarget().getWorkspaceRelations();

    String proposedFileName = data.getVmResource().getLocalVisibleFileName() + " (formatted)";

    MetadataImpl metadata = new MetadataImpl();
    VmResource formattedResource =
        workspaceRelations.createTemporaryFile(metadata, proposedFileName);

    ChromiumDebugPluginUtil.writeFile(formattedResource.getVProjectFile(),
        result.getFormattedText());
  }

  static class ResourceData {
    private final IFile file;
    private final VmResource vmResource;
    private final DebugTargetImpl debugTarget;

    ResourceData(IFile file, VmResource vmResource, DebugTargetImpl debugTarget) {
      this.file = file;
      this.vmResource = vmResource;
      this.debugTarget = debugTarget;
    }

    IFile getFile() {
      return file;
    }

    public VmResource getVmResource() {
      return vmResource;
    }

    public DebugTargetImpl getTarget() {
      return debugTarget;
    }
  }

  /**
   * An empty implementation of {@link VmResource.Metadata}. We don't really need any
   * data so far. We may want to keep a reference to original resource later.
   */
  private static class MetadataImpl implements VmResource.Metadata {

  }
}
