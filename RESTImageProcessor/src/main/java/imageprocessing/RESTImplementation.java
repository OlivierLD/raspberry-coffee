package imageprocessing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import core.PullTxManager;
import http.HTTPServer;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.RESTProcessorUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/**
 * This class defines the REST operations supported by the HTTP Server.
 * <p>
 * This list is defined in the <code>List&lt;Operation&gt;</code> named <code>operations</code>.
 * <br>
 * Those operation mostly retrieve the state of the SunFlower class, and device.
 * <br>
 * The SunFlower will use the {@link #processRequest(Request)} method of this class to
 * have the required requests processed.
 * </p>
 */
@SuppressWarnings("unchecked")
public class RESTImplementation {

	private static boolean verbose = "true".equals(System.getProperty("image.verbose", "false"));
	private final static String IMG_PREFIX = "/img";

	private ImgRequestManager imgRequestManager;

	public RESTImplementation(ImgRequestManager restRequestManager) {

		this.imgRequestManager = restRequestManager;
		// Check duplicates in operation list. Barfs if duplicate is found.
		RESTProcessorUtil.checkDuplicateOperations(operations);
	}

	/**
	 * Define all the REST operations to be managed
	 * by the HTTP server.
	 * <p>
	 * Frame path parameters with curly braces.
	 * <p>
	 * See {@link #processRequest(Request)}
	 * See {@link HTTPServer}
	 */
	private List<Operation> operations = Arrays.asList(
			new Operation(
					"GET",
					IMG_PREFIX + "/oplist",
					this::getOperationList,
					"List of all available operations on the Img service."),
			new Operation(
					"POST",
					IMG_PREFIX + "/download-and-transform",
					this::requestTransformation,
					"Request the download and transform of images (faxes) from the web."),
			new Operation(
					"POST",
					IMG_PREFIX + "/upload",
					this::uploadImage,
					"Request the upload of an image. Image in the body, image name in the header, and Content-Type."),
			new Operation(
					"POST",
					IMG_PREFIX + "/get-from-file-system",
					this::requestTransformation,
					"Same result as for download-and-transform, but will pick already transformed faxes from the file system.")
	);

	protected List<Operation> getOperations() {
		return this.operations;
	}

	/**
	 * This is the method to invoke to have a REST request processed as defined above.
	 *
	 * @param request as it comes from the client
	 * @return the actual result.
	 */
	public Response processRequest(Request request) throws UnsupportedOperationException {
		Optional<Operation> opOp = operations
				.stream()
				.filter(op -> op.getVerb().equals(request.getVerb()) && RESTProcessorUtil.pathMatches(op.getPath(), request.getPath()))
				.findFirst();
		if (opOp.isPresent()) {
			Operation op = opOp.get();
			request.setRequestPattern(op.getPath()); // To get the prms later on.
			Response processed = op.getFn().apply(request); // Execute here.
			return processed;
		} else {
			throw new UnsupportedOperationException(String.format("%s not managed", request.toString()));
		}
	}

	private Response getOperationList(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		List<Operation> opList = this.getOperations();
		String content = new Gson().toJson(opList);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());
		return response;
	}

	// Experimental
	private Response uploadImage(Request request) {
		Response response = new Response(request.getProtocol(), Response.CREATED);
		if (request.getContent() != null && request.getContent().length > 0) {
			byte[] imageData = request.getContent();
			// Content-Type
			String contentType = request.getHeaders().get("Content-Type").trim();
			if (contentType == null) {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("IMG-0011")
								.errorMessage("Request header Content-Type not found"));
				return response;
			}
			String imageFileName = request.getHeaders().get("Image-File-Name");
			if (imageFileName == null) {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("IMG-0012")
								.errorMessage("Request header Image-File-Name not found"));
				return response;
			}

			if (imageData != null) {
				System.out.println(String.format("Image: %d bytes.", imageData.length));
				System.out.println(String.format("Content-Length: %s", request.getHeaders().get("Content-Length")));
				// Temp
				try {
					OutputStream os = new FileOutputStream("tempImageFile.jpg");
					// Starts writing the bytes in it
					os.write(imageData);
					System.out.println("Successfully created image file");
					// Close the file
					os.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				//
				try {
					ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
					BufferedImage bImage2 = ImageIO.read(bis);
					if (bImage2 != null) {
						// Image format from Content-Type
						String imageFormat = "jpg"; // TODO A function to get that one.
						ImageIO.write(bImage2, imageFormat, new File(imageFileName));
						System.out.println("image created");
					} else {
						System.out.println("Something went wrong with the image..., it's null.");
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("IMG-0010")
							.errorMessage("Request required payload (image) not found"));
			return response;
		}

		return response;
	}

	/**
	 * The payload is a list of requests, like this
	 *
	 * [
			 {
				 "url": "http://img.url",
				 "storage": "store/here",   <- Original
				 "returned": "return/this", <- transformed as requested
				 "transparent": "WHITE",
				 "from": "BLACK",
				 "to": "BLUE",
	       "imgType": "png",
				 "tx": "BLUR"
			 }
		 ]
	 * @param request
	 * @return
	 */
	private Response requestTransformation(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		if (request.getContent() != null && request.getContent().length > 0) {
			String payload = new String(request.getContent());
			if (!"null".equals(payload)) {
				if (verbose) {
					System.out.println(String.format("Tx Request: %s", payload));
				}
				Gson gson = new GsonBuilder().create();
				StringReader stringReader = new StringReader(payload);
				List<Object> txRequests = null;
				final List<Object> resultList = new ArrayList<>();
				try {
					txRequests = gson.fromJson(stringReader, List.class);
					txRequests.forEach(json -> {
								try {
									JsonObject jObj = gson.toJsonTree(json).getAsJsonObject();
									PullTxManager.TxRequest txRequest = gson.fromJson(jObj.toString(), PullTxManager.TxRequest.class);
									// Do the job here
									if (verbose) {
										System.out.println(String.format("Downloading %s...", txRequest.toString()));
									}
									if (txRequest.getUrl().startsWith("file:")) { // Comes from an existing composite
										txRequest = txRequest.returned(txRequest.getUrl());
									}
									PullTxManager.downloadAndTransform(
											txRequest.getUrl(),
											txRequest.getStorage(),
											txRequest.getReturned(),
											txRequest.getTransparent().color(),
											txRequest.getFrom() == null ? null : txRequest.getFrom().color(),
											txRequest.getTo() == null ? null : txRequest.getTo().color(),
											txRequest.getImgType(),
											txRequest.getTx().type(),
											Math.toRadians(txRequest.getRotation()));
									if (verbose) {
										System.out.println(String.format("Done with %s... final document is %s", txRequest.getUrl(), txRequest.getReturned()));
									}
									resultList.add(txRequest);
								} catch (Exception ex) {
									// Return this
									ex.printStackTrace();
									resultList.add(ex);
								}
							});
					String content = new Gson().toJson(resultList);
					RESTProcessorUtil.generateResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				} catch (Exception ex) {
					ex.printStackTrace();
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("IMG-0001")
									.errorMessage(ex.toString()));
					return response;
				}
			} else {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("IMG-0002")
								.errorMessage("Request payload not found"));
				return response;
			}
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("IMG-0002")
							.errorMessage("Request payload not found"));
			return response;
		}

		return response;
	}

	/**
	 * Can be used as a temporary placeholder when creating a new operation.
	 *
	 * @param request
	 * @return
	 */
	private Response emptyOperation(Request request) {
		Response response = new Response(request.getProtocol(), Response.NOT_IMPLEMENTED);
		return response;
	}
}
