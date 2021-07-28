from setuptools import setup

setup(
    name='python-tests',
    author='Michael Hillman',
    author_email='mdhillman@cmclinnovations.com',
    description='An example of a python package from pre-existing code',
	version='0.0.1',
	package_dir={"":"."},
    packages=['availability', 'utils']
)