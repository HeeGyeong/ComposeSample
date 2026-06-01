# API Creation Guideline

## Purpose
This document provides a guideline for adding API call logic that follows the MVVM architecture and Material3 guidelines, based on an API document.

When using generative AI, add the guideline below and then write the prompt.

## Requirements

1. **API call function**
   - Write an API call function that matches the API document.

2. **Guideline**
   - Follow the rules stated in `Create API and UI Binding Guide`.

## Constraints

1. **Imports**
   - Check the existing versions and use imports that match them.

2. **Responsiveness**
   - The code must be responsive.

3. **API document**
   - An API document is required for implementation.

## Expected Result

1. **API interface**
   - The API function is added to the API interface.
   - Explore the API interface and add the function in the appropriate place.

2. **ViewModel**
   - The API call function exists in the appropriate ViewModel.
   - Avoid creating a new ViewModel when possible.

3. **Flow implementation**
   - Add a flow that matches the API document.
   - Since the response is not needed, receive it as the `Any` type.

## Additional Notes

- Keep all implementation consistent with the project's existing patterns and rules.
- Follow the MVVM architecture and Material3 guidelines.
