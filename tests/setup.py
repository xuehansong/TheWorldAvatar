from setuptools import setup

setup(
    # Needed to silence warnings (and to be a worthwhile package)
    name='twa-tests',
    url='https://github.com/cambridge-cares/TheWorldAvatar/tree/develop/tests',
    author='Michael Hillman',
    author_email='mdhillman@cmclinnovations.com',
    packages=['availability', 'utils'],
    version='1.0.0-SNAPSHOT',
    description='An example of a python package from pre-existing code',
)