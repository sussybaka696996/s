package net.kdt.pojavlaunch;

import android.graphics.*;
import android.os.*;
import android.support.v7.app.*;
import android.view.*;
import com.oracle.dalvik.*;
import java.io.*;
import java.util.*;

public class InstallModActivity extends AppCompatActivity
{
	private TextureView mTextureView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.install_mod);
		
		final File modFile = (File) getIntent().getExtras().getSerializable("modFile");
		
		mTextureView = findViewById(R.id.installmod_surfaceview);
		mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener(){

				@Override
				public void onSurfaceTextureAvailable(SurfaceTexture tex, int w, int h) {
					BinaryExecutor.setupBridgeSurfaceAWT(new Surface(tex));
					new Thread(new Runnable(){
							@Override
							public void run() {
								launchJavaRuntime(modFile);
								finish();
							}
						}).start();
				}

				@Override
				public boolean onSurfaceTextureDestroyed(SurfaceTexture tex) {
					return false;
				}

				@Override
				public void onSurfaceTextureSizeChanged(SurfaceTexture tex, int w, int h) {
					
				}

				@Override
				public void onSurfaceTextureUpdated(SurfaceTexture tex) {
					
				}
			});
	}
	
	public void forceClose(View v) {
		MainActivity.dialogForceClose(this);
	}
	
	private void launchJavaRuntime(File modFile) {
		try {
			List<String> javaArgList = new ArrayList<String>();

			javaArgList.add(Tools.homeJreDir + "/bin/java");

			// javaArgList.add("-Xms512m");
			javaArgList.add("-Xmx512m");

			javaArgList.add("-Djava.home=" + Tools.homeJreDir);
			javaArgList.add("-Dos.name=Linux");
			javaArgList.add("-Djava.library.path=");
			javaArgList.add("-jar");
			javaArgList.add(modFile.getAbsolutePath());
			
			String libName = System.getProperty("os.arch").contains("64") ? "lib64" : "lib";
			String ldLibraryPath = (
				// To make libjli.so ignore re-execute
				Tools.homeJreDir + "/lib/server:" +

				"/system/" + libName + ":" +
				"/vendor/" + libName + ":" +
				"/vendor/" + libName + "/hw:" +

				getApplicationInfo().nativeLibraryDir + ":" +

				Tools.homeJreDir + "/lib/jli:" +
				Tools.homeJreDir + "/lib"
			);
			
			BinaryExecutor.setJavaEnvironment();
			BinaryExecutor.redirectStdio();
			BinaryExecutor.setLdLibraryPath(ldLibraryPath);
			BinaryExecutor.initJavaRuntime();
			BinaryExecutor.chdir(Tools.MAIN_PATH);

			VMLauncher.launchJVM(javaArgList.toArray(new String[0]));
		} catch (Throwable th) {
			Tools.showError(this, th, true);
		}
	}
}